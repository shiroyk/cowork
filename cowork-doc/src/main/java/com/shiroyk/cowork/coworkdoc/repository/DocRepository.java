package com.shiroyk.cowork.coworkdoc.repository;

import com.shiroyk.cowork.coworkcommon.model.doc.Doc;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DocRepository extends MongoRepository<Doc, String> {

    @Query("{ 'delete': false, 'title': /?0/, 'owner.id': ?1 }")
    List<Doc> findDocsByTitleAndOwnerAndDeleteFalse(String title, String owner);

    @Query("{ 'delete': true, 'title': /?0/, 'owner.id': ?1 }")
    List<Doc> findDocsByTitleAndOwnerAndDeleteTrue(String title, String owner);

    @Query("{ 'delete': false, 'owner.id': ?0 }")
    List<Doc> findDocsByDeleteFalseAndOwner(String owner, Pageable pageable);

    @Query("{ 'delete': true, 'owner.id': ?0 }")
    List<Doc> findDocsByDeleteTrueAndOwner(String owner, Pageable pageable);

    @Query(value = "{ 'delete': false, 'owner.id': ?0 }", count = true)
    Long countDocsByDeleteFalseAndOwner(String owner);

    @Query(value = "{ 'delete': true, 'owner.id': ?0 }", count = true)
    Long countDocsByDeleteTrueAndOwner(String owner);

    @Query("{ 'url.url': ?0, 'delete': false }")
    Optional<Doc> findDocByDeleteAndUrl(String url);
}
