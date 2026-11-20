package com.elemental.tech.workitems.service;

import com.elemental.tech.workitems.api.dto.CreateWorkItemRequest;
import com.elemental.tech.workitems.api.dto.WorkItemResponse;
import com.elemental.tech.workitems.domain.WorkItem;
import com.elemental.tech.workitems.persistence.WorkItemRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkItemService {

    private final WorkItemRepository repository;

    public WorkItemService(WorkItemRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WorkItemResponse create(CreateWorkItemRequest request) {
        WorkItem workItem = new WorkItem(
                request.title(),
                request.description(),
                WorkItem.Priority.valueOf(request.priority().name())
        );

        WorkItem saved = repository.save(workItem);
        return WorkItemMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkItemResponse> list() {
        return repository.findAll()
                .stream()
                .map(WorkItemMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkItemResponse get(long id) {
        WorkItem workItem = repository.findById(id)
                // TODO: add global exception mapping.
                .orElseThrow(() -> new IllegalArgumentException("work item not found: " + id));
        return WorkItemMapper.toResponse(workItem);
    }

    @Transactional
    public WorkItemResponse update(long id, WorkItem.Status newStatus, long expectedVersion) {
        WorkItem workItem = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("work item not found: " + id));

        if(!workItem.getVersion().equals(expectedVersion)){
           throw new OptimisticLockException("WorkItem id= + " + workItem.getId() + " has already been updated to a new version");
        }

        workItem.transitionTo(newStatus);
        WorkItem savedWorkItem = repository.save(workItem);
        return WorkItemMapper.toResponse(savedWorkItem);

    }

    @Transactional(readOnly = true)
    public Page<WorkItemResponse> findByStatus(WorkItem.Status status, Pageable pageable) {
        return repository.findByStatus(status, pageable)
                .map(WorkItemMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<WorkItemResponse> findByPriority(WorkItem.Priority priority, Pageable pageable) {
        return repository.findByPriority(priority, pageable)
                .map(WorkItemMapper::toResponse);
    }

}
