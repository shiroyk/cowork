package com.shiroyk.cowork.coworkdoc.service;

import com.shiroyk.cowork.coworkcommon.crdt.CRDT;
import com.shiroyk.cowork.coworkcommon.crdt.Version;
import com.shiroyk.cowork.coworkcommon.dto.Operation;
import com.shiroyk.cowork.coworkdoc.dto.DocContent;
import com.shiroyk.cowork.coworkdoc.model.DocNode;
import com.shiroyk.cowork.coworkdoc.repository.DocNodeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@AllArgsConstructor
public class DocNodeService {
    private final DocNodeRepository docNodeRepository;

    @Async
    public void applyOps(Operation ops) {
        Arrays.stream(ops.getCrdts()).forEach(crdt -> {
            switch (crdt.type()) {
                case insert:
                    this.insert(ops.getDid(), ops.getUid(), crdt);
                    break;
                case format:
                    this.format(ops.getDid(), crdt);
                    break;
                case delete:
                    this.delete(ops.getDid(), crdt);
                    break;
            }
        });
    }

    @Async
    public void saveUploadDoc(Operation ops) {
        Version preVer = new Version(ops.getUid(), 0);
        Version latestVer = new Version(ops.getUid(), 0);
        List<DocNode> nodes = new ArrayList<>();

        for (int i = 0; i < ops.getCrdts().length; i++) {
            CRDT crdt = ops.getCrdts()[i];
            if (crdt.getInsert() instanceof String) {
                for (char c : ((String) crdt.getInsert()).toCharArray()) {
                    latestVer = latestVer.increase();
                    nodes.add(new DocNode(ops.getDid(), c, crdt.getAttributes(), latestVer, preVer));
                    preVer = latestVer;
                }
            } else {
                latestVer = latestVer.increase();
                nodes.add(new DocNode(ops.getDid(), crdt.getInsert(), crdt.getAttributes(), latestVer, preVer));
                preVer = latestVer;
            }
        }
        this.save(nodes);
    }

    public void insert(String docId, String uid, CRDT crdt) {
        Version preVer = this.getPreNodeVersion(docId, crdt.getVersion());

        if (preVer == null) {
            //这里需要将找到不到的Version放入到集合，下次再进行重复操作？
            log.info("can't find version {}", crdt.getVersion());
            return;
        }

        Version latestVer = this.getLatestVersion(docId, uid);

        log.info("insert {} length {} preVer {} latestVer {} ", crdt, crdt.length(), preVer, latestVer);

        if (crdt.length() > 1) {
            List<DocNode> nodes = new ArrayList<>();
            for (char c : ((String) crdt.getInsert()).toCharArray()) {
                latestVer = latestVer.increase();
                nodes.add(new DocNode(docId, c, crdt.getAttributes(), latestVer, preVer));
                preVer = latestVer;
            }
            this.save(nodes);
        } else {
            DocNode newNode = new DocNode(docId, crdt.getInsert(), crdt.getAttributes(), latestVer.increase(), preVer);
            this.save(newNode);
        }
    }

    public void format(String docId, CRDT crdt) {
        int from = crdt.getVersion().getVersion(), to = from + crdt.getFormat() - 1;
        log.debug("format {} from {} to {}", crdt, from, to);
        List<Integer> version = IntStream.rangeClosed(from, to)
                .boxed().collect(Collectors.toList());
        List<DocNode> nodes = this.findDocNodesByDocIdAndVersionIn(docId, version).stream()
                .peek(docNode -> {
                    if (docNode.getAttributes() != null)
                        docNode.getAttributes().putAll(crdt.getAttributes());
                    else
                        docNode.setAttributes(crdt.getAttributes());
                    docNode.getAttributes().values().removeAll(Collections.singleton(null));
                })
                .collect(Collectors.toList());

        log.debug("format {}", nodes);

        this.save(nodes);
    }

    public void delete(String docId, CRDT crdt) {
        int from = crdt.getVersion().getVersion(), to = from + crdt.getDelete() - 1;
        log.debug("delete {} from {} to {}", crdt, from, to);
        List<Integer> version = IntStream.rangeClosed(from, to)
                .boxed().collect(Collectors.toList());
        List<DocNode> nodes = this.findDocNodesByDocIdAndVersionIn(docId, version)
                .stream().peek(node -> node.setTombstone(true))
                .collect(Collectors.toList());

        log.debug("delete {}", nodes);

        this.save(nodes);
    }

    public long countNodesByDoc(String docId) {
        return docNodeRepository.countDocNodesByDocId(docId);
    }

    public Version getLatestVersion(String id, String uid) {
        DocNode latestNode = docNodeRepository.findFirstByDocId(id, Sort.by("version.version").descending());
        return latestNode == null ? Version.head().copy(uid) : latestNode.getVersion().copy(uid);
    }

    public Version getPreNodeVersion(String docId, Version version) {
        DocNode preNode = this.findDocNodeByVersion(docId, version);
        if (preNode == null && this.countNodesByDoc(docId) == 0 || version.getVersion() == 0)
            return Version.head();
        return preNode == null ? null : preNode.getVersion();
    }

    public DocNode findDocNodeByVersion(String docId, Version version) {
        return docNodeRepository.findDocNodeByDocIdAndVersion(docId, version);
    }

    public Optional<DocNode> findById(String id) {
        return docNodeRepository.findById(id);
    }

    public DocContent getDocContent(String docId, int size) {
        DocNode latestNode = docNodeRepository.findFirstByDocId(docId, Sort.by("version.version").descending());
        Version latestVer = latestNode == null ? Version.head() : latestNode.getVersion();
        return new DocContent(latestVer, this.findDocNodesByDocIdAndTombstoneFalse(docId));
    }

    public List<DocNode> findDocNodesByDocId(String docId) {
        return docNodeRepository.findDocNodesByDocId(docId);
    }

    public List<DocNode> findDocNodesByDocIdAndVersionIn(String docId, List<Integer> version) {
        return docNodeRepository.findDocNodesByDocIdAndVersionIn(docId, version);
    }

    public List<DocNode> findDocNodesByDocIdAndTombstoneVersion(String docId, Pageable pageable) {
        return docNodeRepository.findDocNodesByDocIdAndTombstoneVersion(docId, pageable);
    }

    public List<DocNode> findDocNodesByDocIdAndTombstoneFalse(String docId) {
        return docNodeRepository.findDocNodesByDocIdAndTombstoneIsFalse(docId);
    }

    public void save(DocNode node) {
        docNodeRepository.save(node);
    }

    public void save(Iterable<DocNode> nodeIterable) {
        docNodeRepository.saveAll(nodeIterable);
    }

    @Async
    public void deleteAll(String id) {
        docNodeRepository.deleteAll(docNodeRepository.findDocNodesByDocId(id));
    }
}
