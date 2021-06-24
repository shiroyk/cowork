package com.shiroyk.cowork.coworkadmin.repository.doc;

import com.shiroyk.cowork.coworkcommon.model.doc.Doc;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocRepository extends MongoRepository<Doc, String> {
    @Query("{ 'delete': false, 'owner.id': ?0 }")
    List<Doc> findDocsByDeleteFalseAndOwner(String owner, Pageable pageable);

    @Query("{ 'owner.id': ?0 }")
    List<Doc> findDocsByOwner(String owner, Pageable pageable);
}
