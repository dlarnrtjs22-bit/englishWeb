package com.nativeflow.backend.controller;

import com.nativeflow.backend.common.security.AuthenticatedUser;
import com.nativeflow.backend.common.security.CurrentUser;
import com.nativeflow.backend.settings.dto.SettingsDtos;
import com.nativeflow.backend.settings.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public SettingsDtos.SettingsResponse getSettings(@CurrentUser AuthenticatedUser authenticatedUser) {
        return settingsService.getSettings(authenticatedUser.userId());
    }

    @PatchMapping
    public SettingsDtos.SettingsResponse updateSettings(
            @CurrentUser AuthenticatedUser authenticatedUser,
            @Valid @RequestBody SettingsDtos.UpdateSettingsRequest request
    ) {
        return settingsService.updateSettings(authenticatedUser.userId(), request);
    }
}
