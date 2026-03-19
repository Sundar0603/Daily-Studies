package com.dailystudies.service;

import com.dailystudies.model.Topic;
import com.dailystudies.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class TopicService {

    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    /**
     * Fetches all topics, auto-increments count for every day missed since
     * last_updated_date, persists the updated dates/counts, and returns the list.
     */
    public List<Topic> getAllTopics() {
        List<Topic> topics = topicRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Topic topic : topics) {
            long daysMissed = ChronoUnit.DAYS.between(topic.getLastUpdatedDate(), today);
            if (daysMissed > 0) {
                topic.setCount(topic.getCount() + (int) daysMissed);
                topic.setLastUpdatedDate(today);
            }
        }

        topicRepository.saveAll(topics);
        return topics;
    }

    /**
     * Updates count, itemToStudyNext, and lastUpdatedDate for a topic.
     */
    public Topic updateTopic(String topicName, int count, String itemToStudyNext) {
        Topic topic = topicRepository.findById(topicName)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicName));
        topic.setCount(count);
        topic.setLastUpdatedDate(LocalDate.now());
        topic.setItemToStudyNext(itemToStudyNext);
        return topicRepository.save(topic);
    }

    public Topic addTopic(String topicName) {
        if (topicRepository.existsById(topicName)) {
            throw new IllegalArgumentException("Topic already exists: " + topicName);
        }
        return topicRepository.save(new Topic(topicName));
    }

    public void removeTopic(String topicName) {
        topicRepository.deleteById(topicName);
    }
}
