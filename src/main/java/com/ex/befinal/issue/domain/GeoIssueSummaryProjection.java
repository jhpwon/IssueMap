package com.ex.befinal.issue.domain;

import java.util.Date;

public interface GeoIssueSummaryProjection extends IssueSummaryProjection {
  Long getId();

  String getTitle();

  String getThumbnailUrl();

  String getDescription();

  String getTags();

  Date getCreatedAt();

  Double getDistance();

  Double getLatitude();

  Double getLongitude();

}