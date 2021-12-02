package com.practice.notify.service;

import com.practice.notify.email.SendEmailJob;
import com.practice.notify.model.ClientRC;
import com.practice.notify.repository.ClientRCRepo;
import lombok.AllArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@Service
public class ClientRCService {

    private final ClientRCRepo rcRepository;
    private final Scheduler scheduler;

    public CompletableFuture<ClientRC> createClient(ClientRC clientRC) throws SchedulerException {
        CompletableFuture<ClientRC> result = rcRepository.nextId()
                .thenApply(id -> new ClientRC(id, clientRC.getName(), clientRC.getEmail()))
                .thenCompose(rcRepository::create);
        scheduleEmailJob(clientRC);
        return result;
    }

    public CompletableFuture<Optional<ClientRC>> updateClient(String id, ClientRC clientRC) {
        return rcRepository.findById(id)
                .thenCompose(optionalClient -> optionalClient
                        .map(client -> rcRepository
                                .create(new ClientRC(id, client.getName(), clientRC.getEmail()))
                                .thenApply(Optional::of))
                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));
    }

    public CompletableFuture<Optional<ClientRC>> retrieveClient(String id) {
        return rcRepository.findById(id);
    }

    public CompletableFuture<Optional<ClientRC>> retrieveClientByEmail(String email) {
        return rcRepository.findByEmail(email);
    }

    public CompletableFuture<Optional<ClientRC>> deleteClient(String id) {
        return rcRepository.remove(id);
    }

    private void scheduleEmailJob(ClientRC clientRC) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob().ofType(SendEmailJob.class)
                .storeDurably()
                .withIdentity(UUID.randomUUID().toString(), "CLIENT_NOTIFY")
                .withDescription("Send client reminder")
                .build();

        jobDetail.getJobDataMap().put("id", clientRC.getId());
        Set<CronTrigger> triggers = new HashSet<>();
        Date scheduleFirstDate = Date.from(Instant.now().plusSeconds(120));
        String cronExpressionOne = convertDateToCronExpression(scheduleFirstDate);

        CronTrigger triggerOne = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(UUID.randomUUID().toString(), "CLIENT_NOTIFY")
                .withDescription("Trigger first notification")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpressionOne))
                .build();

        triggers.add(triggerOne);

        Date scheduleSecondDate = Date.from(Instant.now().plusSeconds(180));
        String cronExpressionTwo = convertDateToCronExpression(scheduleSecondDate);

        CronTrigger triggerTwo = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(UUID.randomUUID().toString(), "CLIENT_NOTIFY")
                .withDescription("Trigger second notification")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpressionTwo))
                .build();

        triggers.add(triggerTwo);
        scheduler.scheduleJob(jobDetail, triggers, true);
    }

    private String convertDateToCronExpression(Date date) {

        Calendar calendar = new GregorianCalendar();

        if (date == null) return null;

        calendar.setTime(date);

        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH) + 1;
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = calendar.get(java.util.Calendar.MINUTE);

        return String.format("0 %d %d %d %d ? %d", minute, hour, day, month, year);
    }
}
