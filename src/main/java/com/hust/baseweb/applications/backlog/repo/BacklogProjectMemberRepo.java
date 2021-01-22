package com.hust.baseweb.applications.backlog.repo;

import com.hust.baseweb.applications.backlog.entity.BacklogProjectMember;
import com.hust.baseweb.entity.UserLogin;
import org.hibernate.annotations.Type;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BacklogProjectMemberRepo extends JpaRepository<BacklogProjectMember, UUID> {
    BacklogProjectMember save(BacklogProjectMember backlogProjectMember);
    List<BacklogProjectMember> findAllByBacklogProjectId(UUID backlogProjectId);
    List<BacklogProjectMember> findAllByMemberPartyId(UUID partyId);
    boolean existsByBacklogProjectIdAndMemberPartyId(UUID backlogProjectId, UUID memberPartyId);
}
