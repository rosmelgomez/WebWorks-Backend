package com.upc.webworksbackend.repository;
import com.upc.webworksbackend.model.RepositoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositoryRepository extends JpaRepository<RepositoryModel, Integer> {
    List<RepositoryModel> findAllByUserRepository_Id(Integer userId);
    long countByUserRepository_Id(Integer userId);
}
