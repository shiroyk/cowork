package com.shiroyk.cowork.coworkdoc.repository;

import com.shiroyk.cowork.coworkcommon.crdt.Version;
import com.shiroyk.cowork.coworkdoc.model.DocNode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DocNodeRepository extends MongoRepository<DocNode, String> {

    Long countDocNodesByDocId(String docId);

    DocNode findFirstByDocId(String docId, Sort sort);

    @Query("{ 'docId': ?0, 'version': ?1 }")
    DocNode findDocNodeByDocIdAndVersion(String docId, Version version);

    List<DocNode> findDocNodesByDocId(String docId);

    @Query("{ 'docId': ?0, 'version.version': { '$in' : ?1 } }")
    List<DocNode> findDocNodesByDocIdAndVersionIn(String docId, List<Integer> version);

    @Query(value = "{ 'docId': ?0, 'tombstone': true }", fields = "{ 'version' : 1 }", sort = "{ 'version' : -1 }")
    List<DocNode> findDocNodesByDocIdAndTombstoneVersion(String docId, Pageable pageable);

    List<DocNode> findDocNodesByDocIdAndTombstoneIsFalse(String docId);
}
