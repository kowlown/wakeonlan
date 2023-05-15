package com.mystnihon.wakeonlan.service;

import com.mystnihon.wakeonlan.data.collections.WakeupHistoryCollection;
import com.mystnihon.wakeonlan.data.collections.WakeupHistoryCollection_;
import lombok.Getter;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Getter
public class HistoryService {
    protected final Nitrite nitrite;
    private final ObjectRepository<WakeupHistoryCollection> historyRepository;

    public HistoryService(Nitrite nitrite) {
        this.nitrite = nitrite;
        historyRepository = nitrite.getRepository(WakeupHistoryCollection.class);
    }

    public void insert(WakeupHistoryCollection toInsert) {
        ObjectFilter objectFilter = ObjectFilters.and(
            ObjectFilters.eq(WakeupHistoryCollection_.host.getName(), toInsert.getHost()),
            ObjectFilters.eq(WakeupHistoryCollection_.macAddress.getName(), toInsert.getMacAddress()),
            ObjectFilters.eq(WakeupHistoryCollection_.port.getName(), toInsert.getPort())
        );
        Optional.ofNullable(historyRepository.find(objectFilter).firstOrDefault()).ifPresentOrElse(found -> {
            found.setOffsetDateTime(OffsetDateTime.now());
            historyRepository.update(found);
        }, () -> getHistoryRepository().insert(toInsert));
    }

    public List<WakeupHistoryCollection> getLastRecentWakeUp(int nbLastRecent) {
        FindOptions sort = FindOptions.sort(WakeupHistoryCollection_.offsetDateTime.getName(), SortOrder.Descending);
        sort.thenLimit(0, nbLastRecent);
        return historyRepository.find(sort).toList();
    }

    public boolean delete(WakeupHistoryCollection wakeupHistoryCollection) {
        return historyRepository.remove(ObjectFilters.eq(WakeupHistoryCollection_.documentId.getName(), wakeupHistoryCollection.getDocumentId())).getAffectedCount() > 0;
    }
}
