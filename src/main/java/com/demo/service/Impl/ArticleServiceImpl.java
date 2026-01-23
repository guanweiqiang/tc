package com.demo.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.demo.common.exception.article.ElasticSearchException;
import com.demo.mapper.ArticleMapper;
import com.demo.pojo.Article;
import com.demo.model.dto.ArticleSearchListDTO;
import com.demo.pojo.UserContext;
import com.demo.model.vo.ArticleDetailVO;
import com.demo.model.vo.ArticleListVO;
import com.demo.service.*;
import com.demo.common.util.StringUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    public static final String ES_INDEX = "tc";

    @Resource
    private ArticleMapper mapper;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private ArticleLikeService articleLikeService;

    @Resource
    private ArticleFavoriteService articleFavoriteService;

    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;


    @Override
    public boolean add(Article article) {
        article.setCreatedAt(LocalDateTime.now());
        article.setStatus(Byte.valueOf("1"));
        article.setViewCount(0);
        int insert = mapper.insert(article);
        if (insert != 1) {
            log.error("文章添加到mysql中失败");
            return false;
        }

        try {
            elasticsearchClient.index(i -> i
                    .index(ES_INDEX)
                    .id(article.getId().toString())
                    .document(article)
            );
        } catch (IOException e) {
            log.error("文章加入elasticsearch失败！");
        }
        log.info("文章{}加入es成功", article.getId());
        return true;
    }

    @Override
    public boolean update(Article article) {
        article.setUpdatedAt(LocalDateTime.now());

        int update = mapper.update(article);
        if (update != 1) {
            log.error("文章更新到mysql失败，id:{}", article.getId());
            return false;
        }

        try {
            elasticsearchClient.update(i -> i
                            .index(ES_INDEX)
                            .id(article.getId().toString())
                            .doc(article),
                    Article.class);
        } catch (IOException e) {
            log.warn("文章更新到es失败，id:{}", article.getId());
        }
        return true;
    }

    //todo:optimize the count of like favorite comment to a hash in redis
    @Override
    public List<ArticleListVO> searchList(ArticleSearchListDTO dto) {
        BoolQuery.Builder bool = QueryBuilders.bool();
        if (StringUtil.hasText(dto.getKeyword())) {
            bool.must(q -> q.multiMatch(
                    m -> m.fields("title", "content")
                            .query(dto.getKeyword())
            ));
        }

        if (dto.getFrom() != null || dto.getTo() != null) {
            bool.filter(f -> f.range(
                            r -> r.date(
                                    d -> {
                                        d.field("createdAt");

                                        if (dto.getFrom() != null) {
                                            d.gte(dto.getFrom().atStartOfDay().toString());
                                        }

                                        if (dto.getTo() != null) {
                                            d.lte(dto.getTo().atTime(23, 59, 59).toString());
                                        }
                                        return d;
                                    }
                            )
                    )
            );
        }

        int page = Optional.ofNullable(dto.getPage()).orElse(1);
        int size = Optional.ofNullable(dto.getSize()).orElse(10);

        int from = (page - 1) * size;

        Long userId = Optional.ofNullable(UserContext.get()).orElse(-1L);

        try {
            SearchResponse<Article> search = elasticsearchClient.search(builder -> builder
                            .index(ES_INDEX)
                            .query(q -> q.bool(bool.build()))
                            .from(from)
                            .sort(
                                    s -> s.field(
                                            f -> f.field("createdAt")
                                                    .order(SortOrder.Desc)
                                    )
                            ),
                    Article.class
            );
            if (search == null) {
                return null;
            }
            Stream<Article> articleStream = search.hits().hits().stream().map(Hit::source)
                    .filter(Objects::nonNull)
                    .filter(a -> Objects.nonNull(a.getId()));
            List<Article> articles = articleStream.toList();

            List<Long> articleIdList = articles.stream()
                    .map(Article::getId)
                    .toList();

            Set<Long> authorIds = articles.stream()
                    .map(Article::getAuthorId)
                    .collect(Collectors.toSet());

            Map<Long, Integer> likeCountBatch = articleLikeService.getLikeCountBatch(articleIdList);
            log.debug("likeCountBatch={}", likeCountBatch);


            Map<Long, String> nicknameBatch = userService.getNicknameBatch(authorIds);
            log.debug("nicknameBatch={}", nicknameBatch);

            Map<Long, Integer> commentCountBatch = commentService.getCommentCountBatch(articleIdList);
            log.debug("commentCountBatch={}", commentCountBatch);

            Map<Long, Integer> favoriteCountBatch = articleFavoriteService.getFavoriteCountBatch(articleIdList);
            log.debug("favoriteCountBatch={}", favoriteCountBatch);


            return articles.stream()
                    .map(article -> {
                        ArticleListVO articleListVO = new ArticleListVO();
                        BeanUtil.copyProperties(article, articleListVO);
                        Long id = article.getId();
                        Long authorId = article.getAuthorId();
                        articleListVO.setLikeCount(likeCountBatch.get(id));
                        articleListVO.setAuthorName(nicknameBatch.get(authorId));
                        articleListVO.setCommentCount(commentCountBatch.get(id));
                        articleListVO.setFavoriteCount(favoriteCountBatch.get(id));
                        articleListVO.setIsFavorite(articleFavoriteService.isFavorite(id, userId));
                        articleListVO.setIsLiked(articleLikeService.isLiked(id, userId));
                        return articleListVO;
                    }).toList();
        } catch (IOException e) {
            log.error("查询失败！");
            throw new ElasticSearchException("查询失败");
        }

    }

    @Override
    public ArticleDetailVO searchDetail(Long id) {
        ArticleDetailVO articleDetailVO = new ArticleDetailVO();
        Article article = mapper.selectById(id);
        BeanUtil.copyProperties(article, articleDetailVO);
        articleDetailVO.setLikeCount(articleLikeService.getLikeCount(id));
        articleDetailVO.setCommentCount(getCommentCount(id));
        articleDetailVO.setAuthorName(getAuthorName(article.getAuthorId()));
        return articleDetailVO;
    }

    @Override
    public Integer getCommentCount(Long id) {
        return mapper.getCommentCount(id);
    }



    @Override
    public String getAuthorName(Long userId) {
        return mapper.getAuthorName(userId);
    }

    @Override
    public boolean delete(Long id) {
        return mapper.delete(id) == 1;
    }


}
