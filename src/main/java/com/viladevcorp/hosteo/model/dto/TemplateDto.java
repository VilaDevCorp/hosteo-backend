package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Template;
import com.viladevcorp.hosteo.model.types.CategoryEnum;
import com.viladevcorp.hosteo.model.types.TaskType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class TemplateDto extends BaseEntityDto {

  public TemplateDto(Template template) {
    if (template == null) {
      return;
    }
    BeanUtils.copyProperties(template, this);
  }

  private String name;

  private TaskType type;

  private CategoryEnum category;

  private int duration;

  private List<String> steps = new ArrayList<>();
}