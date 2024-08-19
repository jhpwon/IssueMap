package com.ex.befinal.models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "posts")
public class Post implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id",  referencedColumnName = "id")
  private User user;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
  private List<PostTag> postTags;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
  private List<Attachment> attachments;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "report_count")
  private short reportCount;

  @Column(precision = 18, scale = 15)
  @Comment("위도")
  private BigDecimal latitude;

  @Column(precision = 18, scale = 15)
  @Comment("경도")
  private BigDecimal longitude;

  @Column(name = "created_at")
  private Date createdAt;

  @Column(name = "removed_at")
  private Date removedAt;

  @Column(name = "disable_at")
  private Date disabledAt;

}
