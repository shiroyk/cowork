package com.shiroyk.cowork.coworkadmin.service;

import com.shiroyk.cowork.coworkadmin.repository.doc.DocRepository;
import com.shiroyk.cowork.coworkcommon.model.doc.Doc;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DocService {
    private final DocRepository docRepository;

    public Long count() {
        return docRepository.count();
    }

    public Page<Doc> findAll(Pageable pageable) {
        return docRepository.findAll(pageable);
    }

    public List<Doc> findDocsByDeleteIsFalse(String owner, Pageable pageable) {
        return docRepository.findDocsByDeleteFalseAndOwner(owner, pageable);
    }

    public List<Doc> findDocsByOwnerIs(String owner, Pageable pageable) {
        return docRepository.findDocsByOwner(owner, pageable);
    }

    public Optional<Doc> findById(String id) {
        return docRepository.findById(id);
    }

    public boolean exist(String id) {
        return docRepository.existsById(id);
    }

    public Doc save(Doc doc) {
        return docRepository.save(doc);
    }

    public void delete(String id) {
        docRepository.deleteById(id);
    }
}
