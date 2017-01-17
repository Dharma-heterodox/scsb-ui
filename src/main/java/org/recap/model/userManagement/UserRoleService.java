package org.recap.model.userManagement;

import org.recap.model.jpa.RoleEntity;
import org.recap.model.jpa.UsersEntity;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Created by dharmendrag on 27/12/16.
 */
public interface UserRoleService {

    Page<UsersEntity> searchUsers(UserRoleForm userRoleForm, boolean superAdmin);

    Page<UsersEntity> searchByNetworkId(UserRoleForm userRoleForm, boolean superAdmin);

    Page<RoleEntity> searchByRoleName(UserRoleForm userRoleForm, boolean superAdmin);

    List<Object> getRoles(Integer superAdminRole);

    List<Object> getInstitutions(boolean isSuperAdmin, Integer loginInstitutionId);

    UsersEntity saveNewUserToDB(UserRoleForm userRoleForm);

    UsersEntity saveEditedUserToDB(Integer userId, String networkLoginId, String userDescription, Integer institutionId, List<Integer> roleIds);
}
