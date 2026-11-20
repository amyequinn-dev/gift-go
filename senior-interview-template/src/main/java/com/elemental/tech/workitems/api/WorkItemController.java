package com.elemental.tech.workitems.api;

import com.elemental.tech.workitems.api.dto.CreateWorkItemRequest;
import com.elemental.tech.workitems.api.dto.WorkItemResponse;
import com.elemental.tech.workitems.domain.WorkItem;
import com.elemental.tech.workitems.service.WorkItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
    TODO: please enhance
 */
@RestController
@RequestMapping("/api/work-items")
public class WorkItemController {

    private final WorkItemService service;

    public WorkItemController(WorkItemService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkItemResponse create(@Valid @RequestBody CreateWorkItemRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<WorkItemResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public WorkItemResponse get(@PathVariable long id) {
        return service.get(id);
    }


    // TODO: implement PATCH endpoint for status transition with validation & concur strategy.

    @PatchMapping("/{id}/status")
    public WorkItemResponse update(@PathVariable long id, @RequestParam WorkItem.Status newStatus, @RequestParam long expectedVersion) {

        return service.update(id, newStatus, expectedVersion);

    }

    @GetMapping("/status")
    public Page<WorkItemResponse> findByStatus(
            @RequestParam WorkItem.Status status,
            Pageable pageable) {

        return service.findByStatus(status, pageable);
    }

    @GetMapping("/priority")
    public Page<WorkItemResponse> findByPriority(
            @RequestParam WorkItem.Priority priority,
            Pageable pageable) {

        return service.findByPriority(priority, pageable);
    }


}
