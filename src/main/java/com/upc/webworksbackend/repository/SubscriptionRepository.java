package com.upc.webworksbackend.repository;
import com.upc.webworksbackend.model.SubscriptionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionModel,Integer> {
    @Query("""
            select s from SubscriptionModel s
            join fetch s.planSubscription p
            where s.userSubscription.id = :userId
              and s.dateStart <= :now
              and s.dateEnd >= :now
            order by p.maxNumberRepository desc, p.maxNumberProject desc, s.dateEnd desc
            """)
    List<SubscriptionModel> findActiveByUser(@Param("userId") Integer userId,
                                             @Param("now") Date now);

    /// Subscription by idUser
    List<SubscriptionModel> findSubscriptionModelByUserSubscription_Id(Integer id);
}
