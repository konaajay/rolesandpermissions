package com.project.www.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.www.dto.RoleExtraFieldRequest;
import com.project.www.dto.RoleExtraFieldResponse;
import com.project.www.entity.Role;
import com.project.www.entity.RoleExtraField;
import com.project.www.entity.User;
import com.project.www.entity.UserExtraFieldValue;
import com.project.www.repository.RoleExtraFieldRepository;
import com.project.www.repository.RoleRepository;
import com.project.www.repository.UserExtraFieldValueRepository;
import com.project.www.service.RoleExtraFieldService;
import com.project.www.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleExtraFieldServiceImpl implements RoleExtraFieldService {

    private final RoleExtraFieldRepository roleExtraFieldRepository;
    private final UserExtraFieldValueRepository userExtraFieldValueRepository;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public List<RoleExtraFieldResponse> getExtraFieldsForRole(Long roleId) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        List<RoleExtraField> fields = roleExtraFieldRepository
                .findAllByRoleIdAndTenantIdAndActiveTrueOrderByDisplayOrderAsc(roleId, tenantId);

        List<RoleExtraFieldResponse> responses = new ArrayList<>();
        for (RoleExtraField field : fields) {
            responses.add(mapToResponse(field));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleExtraFieldResponse> getMergedExtraFieldsForRoles(List<Long> roleIds) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<RoleExtraField> fields = roleExtraFieldRepository
                .findAllByRoleIdInAndTenantIdAndActiveTrueOrderByDisplayOrderAsc(roleIds, tenantId);

        Map<String, RoleExtraFieldResponse> mergedMap = new LinkedHashMap<>();
        for (RoleExtraField field : fields) {
            String name = field.getFieldName();
            List<String> fieldOptions = parseOptions(field.getOptionsJson());

            if (mergedMap.containsKey(name)) {
                RoleExtraFieldResponse existing = mergedMap.get(name);
                // Merge required rule
                existing.setRequired(existing.isRequired() || field.getRequired());
                // Merge options without duplicates
                if (fieldOptions != null) {
                    if (existing.getOptions() == null) {
                        existing.setOptions(new ArrayList<>());
                    }
                    for (String opt : fieldOptions) {
                        if (!existing.getOptions().contains(opt)) {
                            existing.getOptions().add(opt);
                        }
                    }
                }
            } else {
                RoleExtraFieldResponse response = RoleExtraFieldResponse.builder()
                        .id(field.getId())
                        .fieldName(field.getFieldName())
                        .label(field.getFieldLabel())
                        .type(field.getFieldType())
                        .required(field.getRequired())
                        .options(fieldOptions != null ? new ArrayList<>(fieldOptions) : null)
                        .build();
                mergedMap.put(name, response);
            }
        }
        return new ArrayList<>(mergedMap.values());
    }

    @Override
    @Transactional
    public RoleExtraFieldResponse createExtraField(Long roleId, RoleExtraFieldRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Optional<RoleExtraField> existing = roleExtraFieldRepository
                .findByRoleIdAndFieldNameAndTenantId(roleId, request.getFieldName(), tenantId);
        if (existing.isPresent()) {
            throw new RuntimeException("Extra field with this name already exists for the role");
        }

        String optionsJson = null;
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            try {
                optionsJson = objectMapper.writeValueAsString(request.getOptions());
            } catch (Exception e) {
                log.error("Error serializing options", e);
            }
        }

        RoleExtraField field = RoleExtraField.builder()
                .tenantId(tenantId)
                .role(role)
                .fieldName(request.getFieldName())
                .fieldLabel(request.getFieldLabel())
                .fieldType(request.getFieldType())
                .required(request.isRequired())
                .optionsJson(optionsJson)
                .displayOrder(request.getDisplayOrder())
                .active(true)
                .build();

        field = roleExtraFieldRepository.save(field);
        return mapToResponse(field);
    }

    @Override
    @Transactional
    public void saveUserExtraFieldValues(User user, Map<String, Object> extraFieldValues) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        if (extraFieldValues == null || extraFieldValues.isEmpty()) {
            return;
        }

        // Get all active extra fields for user's roles
        List<Long> roleIds = new ArrayList<>();
        if (user.getRole() != null) {
            roleIds.add(user.getRole().getId());
        }
        if (user.getRoles() != null) {
            for (Role r : user.getRoles()) {
                if (!roleIds.contains(r.getId())) {
                    roleIds.add(r.getId());
                }
            }
        }

        if (roleIds.isEmpty()) {
            return;
        }

        List<RoleExtraField> fields = roleExtraFieldRepository
                .findAllByRoleIdInAndTenantIdAndActiveTrueOrderByDisplayOrderAsc(roleIds, tenantId);

        // Validation & Save
        for (RoleExtraField field : fields) {
            Object rawVal = extraFieldValues.get(field.getFieldName());
            String valStr = rawVal != null ? rawVal.toString().trim() : "";

            // Validation Rule
            if (field.getRequired() && valStr.isEmpty()) {
                throw new RuntimeException("Validation failed: Extra field '" + field.getFieldLabel() + "' is required.");
            }

            if (!valStr.isEmpty()) {
                // If it is a dropdown, validate that the value is one of the options
                if ("DROPDOWN".equalsIgnoreCase(field.getFieldType())) {
                    List<String> opts = parseOptions(field.getOptionsJson());
                    if (opts != null && !opts.isEmpty() && !opts.contains(valStr)) {
                        throw new RuntimeException("Validation failed: Value '" + valStr + "' is not a valid option for field '" + field.getFieldLabel() + "'.");
                    }
                }

                // Check if value already exists
                Optional<UserExtraFieldValue> existingVal = userExtraFieldValueRepository
                        .findByUserIdAndFieldIdAndTenantId(user.getId(), field.getId(), tenantId);

                if (existingVal.isPresent()) {
                    UserExtraFieldValue ev = existingVal.get();
                    ev.setFieldValue(valStr);
                    userExtraFieldValueRepository.save(ev);
                } else {
                    UserExtraFieldValue newVal = UserExtraFieldValue.builder()
                            .tenantId(tenantId)
                            .user(user)
                            .field(field)
                            .fieldValue(valStr)
                            .build();
                    userExtraFieldValueRepository.save(newVal);
                }
            } else {
                // If it was provided as empty or null, we can clean up any existing saved record if it's not required
                Optional<UserExtraFieldValue> existingVal = userExtraFieldValueRepository
                        .findByUserIdAndFieldIdAndTenantId(user.getId(), field.getId(), tenantId);
                existingVal.ifPresent(userExtraFieldValueRepository::delete);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserExtraFieldValues(Long userId) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        List<UserExtraFieldValue> values = userExtraFieldValueRepository.findAllByUserIdAndTenantId(userId, tenantId);
        Map<String, Object> result = new LinkedHashMap<>();
        for (UserExtraFieldValue val : values) {
            result.put(val.getField().getFieldName(), val.getFieldValue());
        }
        return result;
    }

    @Override
    @Transactional
    public RoleExtraFieldResponse updateExtraField(Long roleId, Long fieldId, RoleExtraFieldRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        RoleExtraField field = roleExtraFieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Extra field not found"));

        if (!field.getRole().getId().equals(roleId) || !field.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied or mismatch");
        }

        if (!field.getFieldName().equals(request.getFieldName())) {
            Optional<RoleExtraField> existing = roleExtraFieldRepository
                    .findByRoleIdAndFieldNameAndTenantId(roleId, request.getFieldName(), tenantId);
            if (existing.isPresent()) {
                throw new RuntimeException("Extra field with this name already exists for the role");
            }
        }

        String optionsJson = null;
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            try {
                optionsJson = objectMapper.writeValueAsString(request.getOptions());
            } catch (Exception e) {
                log.error("Error serializing options", e);
            }
        }

        field.setFieldName(request.getFieldName());
        field.setFieldLabel(request.getFieldLabel());
        field.setFieldType(request.getFieldType());
        field.setRequired(request.isRequired());
        field.setOptionsJson(optionsJson);
        field.setDisplayOrder(request.getDisplayOrder());

        field = roleExtraFieldRepository.save(field);
        return mapToResponse(field);
    }

    @Override
    @Transactional
    public void deleteExtraField(Long roleId, Long fieldId) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        RoleExtraField field = roleExtraFieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Extra field not found"));

        if (!field.getRole().getId().equals(roleId) || !field.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied or mismatch");
        }

        field.setActive(false);
        roleExtraFieldRepository.save(field);
    }

    private RoleExtraFieldResponse mapToResponse(RoleExtraField field) {
        return RoleExtraFieldResponse.builder()
                .id(field.getId())
                .fieldName(field.getFieldName())
                .label(field.getFieldLabel())
                .type(field.getFieldType())
                .required(field.getRequired())
                .options(parseOptions(field.getOptionsJson()))
                .build();
    }

    private List<String> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Error parsing options JSON", e);
            return null;
        }
    }
}
