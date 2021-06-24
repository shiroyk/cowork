package com.shiroyk.cowork.coworkdoc.service;

import com.google.common.collect.Streams;
import com.shiroyk.cowork.coworkcommon.model.doc.Doc;
import com.shiroyk.cowork.coworkdoc.repository.DocRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class DocService {
    private final DocRepository docRepository;

    public Stream<Doc> findAllById(Set<String> idList) {
        return Streams.stream(docRepository.findAllById(idList));
    }

    public List<Doc> searchDeleteFalse(String title, String owner) {
        return docRepository.findDocsByTitleAndOwnerAndDeleteFalse(title, owner);
    }

    public List<Doc> searchDeleteTrue(String title, String owner) {
        return docRepository.findDocsByTitleAndOwnerAndDeleteTrue(title, owner);
    }

    public Long countDocsByDeleteIsFalse(String owner) {
        return docRepository.countDocsByDeleteFalseAndOwner(owner);
    }

    public List<Doc> findDocsByDeleteIsFalse(String owner, Pageable pageable) {
        return docRepository.findDocsByDeleteFalseAndOwner(owner, pageable);
    }

    public Long countDocsByDeleteIsTrue(String owner) {
        return docRepository.countDocsByDeleteTrueAndOwner(owner);
    }

    public List<Doc> findDocsByDeleteIsTrue(String owner, Pageable pageable) {
        return docRepository.findDocsByDeleteTrueAndOwner(owner, pageable);
    }

    public Optional<Doc> findDocByUrl(String url) {
        return docRepository.findDocByDeleteAndUrl(url);
    }

    public Optional<Doc> findById(String id) {
        return docRepository.findById(id);
    }

    public Doc save(Doc doc) {
        return docRepository.save(doc);
    }

    public void delete(String id) {
        docRepository.deleteById(id);
    }
}
