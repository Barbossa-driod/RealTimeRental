package com.safely.batch.connector;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Data
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final int maxNumberOfSecondsToIncreaseMessageVisibility = 60;

    private Boolean isIncreaseVisibilityEnable = Boolean.FALSE;
    private JobContext jobContext;

    public void initDataToIncreaseMessageVisibility(String organizationId, JobContext jobContext) {
        log.info("OrganizationId: {}. Init scheduled task to increase message visibility time each {} seconds.",
                organizationId, maxNumberOfSecondsToIncreaseMessageVisibility);
        this.jobContext = jobContext;
        this.isIncreaseVisibilityEnable = Boolean.TRUE;
    }

    @Scheduled(fixedRate = maxNumberOfSecondsToIncreaseMessageVisibility * 1000)
    public void scheduledMessageVisibilityRefresh() throws Exception {
        if (isIncreaseVisibilityEnable) {
            jobContext.refreshVisibility(maxNumberOfSecondsToIncreaseMessageVisibility);
        }
    }
}
