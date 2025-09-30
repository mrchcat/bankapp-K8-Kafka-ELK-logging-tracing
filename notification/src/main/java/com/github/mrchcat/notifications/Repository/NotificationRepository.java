package com.github.mrchcat.notifications.Repository;

import com.github.mrchcat.notifications.domain.BankNotification;
import com.github.mrchcat.shared.utils.trace.ToTrace;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NotificationRepository extends CrudRepository<BankNotification, Long> {

    @Query("""
            SELECT *
            FROM notifications
            WHERE is_processed=false
            """)
    @ToTrace(spanName = "database", tags = {"database:notifications","operation:find_notifications"})
    List<BankNotification> findAllNotProcessed();

    @Query("""
            UPDATE notifications
            SET is_processed=true
            WHERE id=:id
            RETURNING id
            """)
    @ToTrace(spanName = "database", tags = {"database:notifications","operation:update_notification"})
    long setProcessed(long id);

    @Override
    @ToTrace(spanName = "database", tags = {"database:notifications","operation:save_notification"})
    <S extends BankNotification> S save(S entity);
}
