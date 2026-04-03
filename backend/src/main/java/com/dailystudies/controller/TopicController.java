package com.dailystudies.controller;

import com.dailystudies.model.Topic;
import com.dailystudies.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    // GET /api/topics
    @GetMapping
    public List<Topic> getAllTopics() {
        return topicService.getAllTopics();
    }

    // PUT /api/topics/{topic}
    @PutMapping("/{topic}")
    public Topic updateTopic(
            @PathVariable String topic,
            @RequestBody UpdateRequest body) {
        Topic selectedTopic = topicService.getTopicByName(topic);
        if (body.count() == selectedTopic.getCount()) {
            return topicService.updateTopic(topic, body.itemToStudyNext());
        } else if (body.itemToStudyNext() != selectedTopic.getItemToStudyNext()) {
            return topicService.updateTopic(topic, body.count(), body.itemToStudyNext());
        } else {
            return topicService.updateTopic(topic, body.count());
        }
    }

    // POST /api/topics
    @PostMapping
    public Topic addTopic(@RequestBody AddRequest body) {
        return topicService.addTopic(body.topic());
    }

    // DELETE /api/topics/{topic}
    @DeleteMapping("/{topic}")
    public ResponseEntity<Void> removeTopic(@PathVariable String topic) {
        topicService.removeTopic(topic);
        return ResponseEntity.noContent().build();
    }

    record UpdateRequest(int count, String itemToStudyNext) {
    }

    record AddRequest(String topic) {
    }
}
