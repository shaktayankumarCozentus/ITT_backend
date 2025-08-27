package com.itt.service.controller;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

	private final BuildProperties buildProperties;
	private final GitProperties gitProperties;

	// Use constructor injection with optional dependencies
	public HealthCheckController(
			@Autowired(required = false) BuildProperties buildProperties,
			@Autowired(required = false) GitProperties gitProperties) {
		this.buildProperties = buildProperties;
		this.gitProperties = gitProperties;
	}

	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> healthCheck(
			@RequestParam(required = false, defaultValue = "UTC") String zone) {
		DateTimeFormatter formatter = getFormatter(zone);

		Map<String, Object> healthInfo = new HashMap<>();
		healthInfo.put("status", "UP");
		healthInfo.put("timestamp", formatter.format(Instant.now()));
		healthInfo.put("service", buildProperties != null ? buildProperties.getName() : "Unknown");
		healthInfo.put("version", buildProperties != null ? buildProperties.getVersion() : "Unknown");
		healthInfo.put("buildTime", buildProperties != null && buildProperties.getTime() != null 
			? formatter.format(buildProperties.getTime()) 
			: "Unknown");
		return ResponseEntity.ok(healthInfo);
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> info(@RequestParam(required = false, defaultValue = "UTC") String zone) {
		DateTimeFormatter formatter = getFormatter(zone);

		Map<String, Object> info = new HashMap<>();

		// Build information with null safety
		Map<String, Object> build = new HashMap<>();
		build.put("name", buildProperties != null ? buildProperties.getName() : "Unknown");
		build.put("version", buildProperties != null ? buildProperties.getVersion() : "Unknown");
		build.put("time", buildProperties != null && buildProperties.getTime() != null 
			? formatter.format(buildProperties.getTime()) 
			: "Unknown");

		// Git information with null safety
		Map<String, Object> git = new HashMap<>();
		git.put("commitId", gitProperties != null ? gitProperties.getCommitId() : "Unknown");
		git.put("branch", gitProperties != null ? gitProperties.getBranch() : "Unknown");
		git.put("commitTime", gitProperties != null && gitProperties.getCommitTime() != null 
			? formatter.format(gitProperties.getCommitTime()) 
			: "Unknown");

		info.put("build", build);
		info.put("git", git);
		info.put("environment", System.getProperty("spring.profiles.active", "default"));
		info.put("timestamp", formatter.format(Instant.now()));

		return ResponseEntity.ok(info);
	}

	private DateTimeFormatter getFormatter(String zone) {
		ZoneId zoneId;
		if ("IST".equalsIgnoreCase(zone)) {
			zoneId = ZoneId.of("Asia/Kolkata");
		} else {
			zoneId = ZoneId.of("UTC");
		}
		return DateTimeFormatter.ofPattern("d MMMM, yyyy - h:mm a z").withZone(zoneId).withLocale(Locale.ENGLISH);
	}
}
