package com.luter.heimdall.admin.module.sys.vo;
    import com.luter.heimdall.starter.model.base.AbstractVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;
import java.io.Serializable;
/**
* 字典分类 VO对象
*/
@Data
@Accessors(chain = true)
@ApiModel(value = "字典分类VO对象",description = "字典分类VO对象")
@EqualsAndHashCode(callSuper = true)
    public class SysDictTypeVO extends AbstractVO implements Serializable{

        @ApiModelProperty("")
        private String name ;

}
