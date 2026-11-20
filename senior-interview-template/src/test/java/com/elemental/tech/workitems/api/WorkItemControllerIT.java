package com.elemental.tech.workitems.api;

// TODO: 2. Left imports as a hint.

import com.elemental.tech.workitems.api.dto.WorkItemResponse;
import com.elemental.tech.workitems.domain.WorkItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elemental.tech.workitems.api.dto.CreateWorkItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class WorkItemControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createNewWorkItem_shouldReturnWorkItemResponse() throws Exception {
        var req = new CreateWorkItemRequest("Hello", "World", CreateWorkItemRequest.Priority.MEDIUM);

        // TODO: 1. Please identify an appropriate testing strat
        String response = mvc.perform(post("/api/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        WorkItemResponse workItemResponseCreated = objectMapper.readValue(response, WorkItemResponse.class);

        assertThat(workItemResponseCreated.title()).isEqualTo(req.title());
        assertThat(workItemResponseCreated.description()).isEqualTo(req.description());
        assertThat(workItemResponseCreated.priority()).isEqualTo(req.priority().name());

        // TODO: 3. enhance and add further tests
    }

    @Test
    void getWorkItemById_ShouldReturnWorkItem() throws Exception {
        var req = new CreateWorkItemRequest("title1", "desc1", CreateWorkItemRequest.Priority.HIGH);

        String response = mvc.perform(post("/api/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        WorkItemResponse workItemResponseCreated = objectMapper.readValue(response, WorkItemResponse.class);


        String response2 = mvc.perform(get("/api/work-items/" + workItemResponseCreated.id()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        WorkItemResponse workItemResponse = objectMapper.readValue(response2, WorkItemResponse.class);

        assertThat(workItemResponse.id()).isEqualTo(workItemResponseCreated.id());
    }

    @Test
    void listWorkItems_ShouldReturnListOfWorkItems() throws Exception {
        var req1 = new CreateWorkItemRequest("title3", "desc3", CreateWorkItemRequest.Priority.HIGH);
        var req2 = new CreateWorkItemRequest("title4", "desc4", CreateWorkItemRequest.Priority.HIGH);

        String response1 = mvc.perform(post("/api/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        WorkItemResponse workItemResponse1 = objectMapper.readValue(response1, WorkItemResponse.class);

        String response2 = mvc.perform(post("/api/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        WorkItemResponse workItemResponse2 = objectMapper.readValue(response2, WorkItemResponse.class);

        String response3 = mvc.perform(get("/api/work-items"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        WorkItemResponse[] workItemResponseArray = objectMapper.readValue(response3, WorkItemResponse[].class);
        List<WorkItemResponse> workItemResponseList = Arrays.asList(workItemResponseArray);

        assertThat(workItemResponseList).isNotNull();
        assertThat(workItemResponseList.contains(workItemResponse1)).isTrue();
        assertThat(workItemResponseList.contains(workItemResponse2)).isTrue();

    }

    @Test
    void workItemNotFoundById_ShouldReturnStatusNotFound() throws Exception {

        var id = -1;

        mvc.perform(get("/api/work-items/" + id))
                .andExpect(status().isNotFound());

    }

    @Test
    void updateWorkItemStatus_ShouldReturnUpdatedWorkItem() throws Exception {
        var req = new CreateWorkItemRequest("title5", "desc5", CreateWorkItemRequest.Priority.LOW);
        var newStatus = WorkItem.Status.IN_PROGRESS.name();

        String response = mvc.perform(post("/api/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        WorkItemResponse workItemResponseCreated = objectMapper.readValue(response, WorkItemResponse.class);


        String updated = mvc.perform(patch("/api/work-items/" + workItemResponseCreated.id() + "/status")
                        .param("newStatus", newStatus)
                        .param("expectedVersion", String.valueOf(workItemResponseCreated.version()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        WorkItemResponse workItemResponse = objectMapper.readValue(updated, WorkItemResponse.class);

        assertThat(workItemResponse.status()).isEqualTo(newStatus);


    }

    @Test
    void updateWorkItemStatus_WhenVersionsAreNotMatched_ShouldReturnsHttpStatusConflict() throws Exception {
        var req = new CreateWorkItemRequest("title6", "desc6", CreateWorkItemRequest.Priority.MEDIUM);
        var newStatus = WorkItem.Status.IN_PROGRESS.name();

        String response = mvc.perform(post("/api/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        WorkItemResponse workItemResponseCreated = objectMapper.readValue(response, WorkItemResponse.class);
        var version = workItemResponseCreated.version() + 1;

        mvc.perform(patch("/api/work-items/" + workItemResponseCreated.id() + "/status")
                        .param("newStatus", newStatus)
                        .param("expectedVersion", String.valueOf(version))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

    }

    @Test
    void findByStatus_ShouldReturnOpenWorkItems() throws Exception {

        var req = new CreateWorkItemRequest(
                "title7", "desc7", CreateWorkItemRequest.Priority.HIGH);

        mvc.perform(post("/api/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        String response = mvc.perform(get("/api/work-items/status")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println(response);
        assertThat(response).contains(req.title());
        assertThat(response).contains("\"status\":\"OPEN\"");
        assertThat(response).contains("\"pageable\":");
    }

    @Test
    void findByPriority_ShouldReturnHighPriorityWorkItems() throws Exception {

        var req = new CreateWorkItemRequest(
                "title8", "desc8", CreateWorkItemRequest.Priority.HIGH);

        mvc.perform(post("/api/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        String response = mvc.perform(get("/api/work-items/priority")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println(response);
        assertThat(response).contains(req.title());
        assertThat(response).contains("\"priority\":\"HIGH\"");
        assertThat(response).contains("\"pageable\":");
    }



}
