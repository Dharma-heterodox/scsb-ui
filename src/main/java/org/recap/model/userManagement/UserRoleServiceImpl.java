package org.recap.model.userManagement;

import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.jpa.RoleEntity;
import org.recap.model.jpa.UsersEntity;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.repository.jpa.RolesDetailsRepositorty;
import org.recap.repository.jpa.UserDetailsRepository;
import org.recap.security.UserManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dharmendrag on 23/12/16.
 */
@Service
public class UserRoleServiceImpl implements UserRoleService{

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private RolesDetailsRepositorty rolesDetailsRepositorty;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    public Page<UsersEntity> searchUsers(UserRoleForm userRoleForm, boolean superAdmin)
    {
        Pageable pageable=new PageRequest(userRoleForm.getPageNumber(), userRoleForm.getPageSize(), Sort.Direction.ASC, UserManagement.USER_ID);
        if(superAdmin)
        {
            return userDetailsRepository.findAll(pageable);
        }else{
            InstitutionEntity institutionEntity=new InstitutionEntity();
            institutionEntity.setInstitutionId(userRoleForm.getInstitutionId());
            return userDetailsRepository.findByInstitutionEntity(institutionEntity,pageable);
        }

    }

    public Page<UsersEntity> searchByNetworkId(UserRoleForm userRoleForm, boolean superAdmin)
    {
        Pageable pageable=new PageRequest(userRoleForm.getPageNumber(), userRoleForm.getPageSize(), Sort.Direction.ASC, UserManagement.USER_ID);
        if(superAdmin)
        {
            return userDetailsRepository.findByLoginId(userRoleForm.getSearchNetworkId(),pageable);
        }else
        {
            InstitutionEntity institutionEntity=new InstitutionEntity();
            institutionEntity.setInstitutionId(userRoleForm.getInstitutionId());
            return userDetailsRepository.findByLoginIdAndInstitutionEntity(userRoleForm.getSearchNetworkId(),institutionEntity,pageable);
        }
    }

    public Page<RoleEntity> searchByRoleName(UserRoleForm userRoleForm, boolean superAdmin)
    {
        Pageable pageable=new PageRequest(userRoleForm.getPageNumber(), userRoleForm.getPageSize(), Sort.Direction.ASC, UserManagement.ROLE_ID);
        return rolesDetailsRepositorty.findByRoleName(userRoleForm.getRoleName(),pageable);
    }

    public List<Object> getRoles(Integer superAdminRole)
    {
        List<Object> rolesList=new ArrayList<Object>();

        List<RoleEntity> roleEntities=rolesDetailsRepositorty.findAll();

        for(RoleEntity roleEntity:roleEntities)
        {
            if(!superAdminRole.equals(roleEntity.getRoleId())) {
                Object[] role = new Object[2];
                role[0] = roleEntity.getRoleId();
                role[1] = roleEntity.getRoleName();
                rolesList.add(role);
            }
        }

        return rolesList;
    }

    public List<Object> getInstitutions(boolean isSuperAdmin,Integer loginInstitutionId)
    {
        List<Object> institutions=new ArrayList<Object>();

        Iterable<InstitutionEntity> institutionsList=institutionDetailsRepository.findAll();

        for(InstitutionEntity institutionEntity:institutionsList)
        {
            if(isSuperAdmin || loginInstitutionId.equals(institutionEntity.getInstitutionId())) {
                Object[] inst = new Object[2];
                inst[0] = institutionEntity.getInstitutionId();
                inst[1] = institutionEntity.getInstitutionCode();
                institutions.add(inst);
            }
        }

        return institutions;

    }

    @Transactional
    public UsersEntity saveNewUserToDB(UserRoleForm userRoleForm) {
        UsersEntity usersEntity = new UsersEntity();
        UsersEntity saveUsersEntity = null;

        usersEntity.setLoginId(userRoleForm.getNetworkLoginId());

        InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionId(userRoleForm.getInstitutionId());
        usersEntity.setInstitutionId(institutionEntity.getInstitutionId());
        usersEntity.setInstitutionEntity(institutionEntity);

        List<RoleEntity> roleEntityList = rolesDetailsRepositorty.findByRoleIdIn(userRoleForm.getSelectedForCreate());
        usersEntity.setUserRole(roleEntityList);
        usersEntity.setUserDescription(userRoleForm.getUserDescription());

        String networkLoginId = userRoleForm.getNetworkLoginId();
        Integer institutionId = institutionEntity.getInstitutionId();

        UsersEntity byLoginIdAndInstitutionEntity = userDetailsRepository.findByLoginIdAndInstitutionId(networkLoginId, institutionId);
        if (byLoginIdAndInstitutionEntity == null) {
            saveUsersEntity = userDetailsRepository.saveAndFlush(usersEntity);
            userRoleForm.setMessage("User added successfully");
        } else {
            userRoleForm.setErrorMessage("Users should not be duplicate");
        }

        return saveUsersEntity;
    }

    @Override
    public UsersEntity saveEditedUserToDB(Integer userId, String networkLoginId, String userDescription, Integer institutionId, List<Integer> roleIds) {
        UsersEntity usersEntity = new UsersEntity();
        UsersEntity savedUsersEntity = null;
        UsersEntity checkUserId = userDetailsRepository.findByUserId(userId);
        if(checkUserId != null){
            usersEntity.setUserId(userId);
            usersEntity.setLoginId(networkLoginId);
            usersEntity.setUserDescription(userDescription);
            usersEntity.setInstitutionId(institutionId);
            InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionId(institutionId);
            if(institutionEntity != null){
                InstitutionEntity institutionEntity1 = new InstitutionEntity();
                institutionEntity1.setInstitutionId(institutionEntity.getInstitutionId());
                institutionEntity1.setInstitutionCode(institutionEntity.getInstitutionCode());
                institutionEntity1.setInstitutionName(institutionEntity.getInstitutionName());
                usersEntity.setInstitutionEntity(institutionEntity1);
            }
            List<RoleEntity> roleEntityList = rolesDetailsRepositorty.findByRoleIdIn(roleIds);
            if (roleEntityList != null){
                usersEntity.setUserRole(roleEntityList);
            }
            savedUsersEntity= userDetailsRepository.save(usersEntity);
        }
        return savedUsersEntity;
    }

    /*@Override
    public UsersEntity saveEditedUserToDB(UserRoleForm userRoleForm) {
        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setUserId(userRoleForm.getUserId());
        usersEntity.setLoginId(userRoleForm.getEditNetworkLoginId());
        usersEntity.setUserDescription(userRoleForm.getEditUserDescription());

        InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionId(userRoleForm.getEditInstitutionId());
        InstitutionEntity institutionEntity1 = new InstitutionEntity();
        institutionEntity1.setInstitutionId(institutionEntity.getInstitutionId());
        institutionEntity1.setInstitutionCode(institutionEntity.getInstitutionCode());
        institutionEntity1.setInstitutionName(institutionEntity.getInstitutionName());
        usersEntity.setInstitutionEntity(institutionEntity1);

        List<RoleEntity> roleEntityList = rolesDetailsRepositorty.findByRoleIdIn(userRoleForm.getEditSelectedForCreate());
        usersEntity.setUserRole(roleEntityList);

        UsersEntity savedUsersEntity = userDetailsRepository.save(usersEntity);

        return savedUsersEntity;

    }*/



}
