package com.drp.notify.service.impl;

import com.drp.notify.dto.NotifyEvent;
import com.drp.notify.entity.RateControl;
import com.drp.notify.repository.RateControlRepository;
import com.drp.notify.service.RateLimitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RateLimitServiceImpl implements RateLimitService {

    private final RateControlRepository rateControlRepository;

    public RateLimitServiceImpl(RateControlRepository rateControlRepository) {
        this.rateControlRepository = rateControlRepository;
    }

    @Override
    public boolean shouldSend(NotifyEvent event, String channel, String recipient) {
        Optional<RateControl> controlOpt = rateControlRepository.findByEventAndChannelAndRecipient(
                event.getEvent(), channel, recipient, event.getProjectId());

        if (controlOpt.isEmpty()) {
            return true;
        }

        RateControl control = controlOpt.get();
        long windowSeconds = Duration.between(control.getWindowStart(), LocalDateTime.now()).getSeconds();

        if (windowSeconds < 60) {
            if (control.getSendCount() >= 1) {
                return false;
            }
        } else if (windowSeconds < 3600) {
            if (control.getSendCount() >= 3) {
                return false;
            }
        }

        return true;
    }

    @Override
    @Transactional
    public void recordSend(NotifyEvent event, String channel, String recipient) {
        RateControl control = rateControlRepository.findByEventAndChannelAndRecipient(
                        event.getEvent(), channel, recipient, event.getProjectId())
                .orElseGet(() -> {
                    RateControl newControl = new RateControl();
                    newControl.setEvent(event.getEvent());
                    newControl.setChannel(channel);
                    newControl.setRecipient(recipient);
                    newControl.setProjectId(event.getProjectId());
                    newControl.setWindowStart(LocalDateTime.now());
                    return newControl;
                });

        control.setLastSendTime(LocalDateTime.now());
        control.setSendCount(control.getSendCount() + 1);

        if (Duration.between(control.getWindowStart(), LocalDateTime.now()).getSeconds() > 3600) {
            control.setWindowStart(LocalDateTime.now());
            control.setSendCount(1);
        }

        if (control.getId() != null) {
            rateControlRepository.updateById(control);
        } else {
            rateControlRepository.insert(control);
        }
    }
}
