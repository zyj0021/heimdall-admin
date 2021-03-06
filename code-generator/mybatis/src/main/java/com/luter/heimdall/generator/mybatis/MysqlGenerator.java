
/*
 *
 *  *
 *  *
 *  *      Copyright 2020-2021 Luter.me
 *  *
 *  *      Licensed under the Apache License, Version 2.0 (the "License");
 *  *      you may not use this file except in compliance with the License.
 *  *      You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *      Unless required by applicable law or agreed to in writing, software
 *  *      distributed under the License is distributed on an "AS IS" BASIS,
 *  *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *      See the License for the specific language governing permissions and
 *  *      limitations under the License.
 *  *
 *  *
 *
 */

package com.luter.heimdall.generator.mybatis;


import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.luter.heimdall.starter.mybatis.base.controller.AbstractMybatisController;
import com.luter.heimdall.starter.mybatis.base.entity.MybatisAbstractEntity;
import com.luter.heimdall.starter.mybatis.base.service.BaseMybatisService;
import com.luter.heimdall.starter.mybatis.base.service.impl.BaseMybatisServiceImpl;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlGenerator {


    private String destPackage;
    private String projectPath;
    private String driverClass;
    private String url;
    private String username;
    private String password;

    public MysqlGenerator setDestPackage(String destPackage) {
        this.destPackage = destPackage;
        return this;
    }

    public MysqlGenerator setProjectPath(String projectPath) {
        this.projectPath = projectPath;
        return this;
    }

    public MysqlGenerator setDriverClass(String driverClass) {
        this.driverClass = driverClass;
        return this;
    }

    public MysqlGenerator setUrl(String url) {
        this.url = url;
        return this;
    }

    public MysqlGenerator setUsername(String username) {
        this.username = username;
        return this;
    }

    public MysqlGenerator setPassword(String password) {
        this.password = password;
        return this;
    }


    private DataSourceConfig initDsc() {
        DataSourceConfig dsc = new DataSourceConfig()
                .setDriverName(driverClass)
                .setUrl(url)
                .setUsername(username)
                .setPassword(password);
        if (null == dsc) {
            throw new RuntimeException("??????????????????");
        }
//        dsc.setTypeConvert(new MySqlTypeConvert() {
//            @Override
//            public DbColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
//                /**
//                 * ???????????????datetime,timestamp??????Date,????????????GlobeConfig????????????DateType
//                 */
//                if (fieldType.toLowerCase().contains("datetime") || fieldType.toLowerCase().contains("timestamp")) {
//                    return DbColumnType.DATE;
//                }
//                return (DbColumnType) super.processTypeConvert(globalConfig, fieldType);
//            }
//        });
        return dsc;
    }

    private GlobalConfig initGc(String moduleName) {
        // ????????????
        return new GlobalConfig().setOutputDir(projectPath + "/src/main/java")
                .setAuthor("Luter")
                //??????????????????
                .setFileOverride(true)
                //??????Date?????????LocalDateTime
//                .setDateType(DateType.SQL_PACK)
                .setOpen(false)
                .setBaseColumnList(true)
                .setBaseResultMap(true)
                .setEntityName(StringUtils.capitalize(moduleName) + "%sEntity")
                .setMapperName(StringUtils.capitalize(moduleName) + "%sMapper")
                .setControllerName(StringUtils.capitalize(moduleName) + "%sController")
                .setServiceName(StringUtils.capitalize(moduleName) + "%sService")
                .setServiceImplName(StringUtils.capitalize(moduleName) + "%sServiceImpl")
                //.setControllerName("%sController");
                //???????????? Swagger2 ??????
                .setSwagger2(true);
    }

    private StrategyConfig initSc(String... tables) {
        return new StrategyConfig().setNaming(NamingStrategy.underline_to_camel)
                .setColumnNaming(NamingStrategy.underline_to_camel)
                //????????????entity
                .setSuperEntityClass(MybatisAbstractEntity.class)
                //????????????mapper
//        strategy.setSuperMapperClass("com.luter.base.mapper.SuperMapper")
                //????????????lombok
                .setEntityLombokModel(true)
                //???@ablefieldT??????????????????????????????????????????
                .setEntityTableFieldAnnotationEnable(true)
                //?????????controller????????????@restcontroller?????????
                .setRestControllerStyle(true)
                .setControllerMappingHyphenStyle(true)
                .setSuperControllerClass(AbstractMybatisController.class)
                .setSuperServiceClass(BaseMybatisService.class)
                .setSuperServiceImplClass(BaseMybatisServiceImpl.class)
                .setEntitySerialVersionUID(true)
                .setInclude(tables)
                //??????????????????????????????
                .setSuperEntityColumns("created_time", "created_by",
                        "last_modified_by", "last_modified_time", "id", "remarks", "version")
                .setControllerMappingHyphenStyle(false)
                //boolean???????????????is??????
                .setEntityBooleanColumnRemoveIsPrefix(true)
                //????????????
                .setTablePrefix("clt_", "m_", "c_", "pet_", "demo_");
    }

    private InjectionConfig initIc(PackageConfig pc, String classDesc) {
        // ???????????????
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<>(1);
                map.put("classDesc", classDesc);
                this.setMap(map);
            }
        };

        // ????????????????????? freemarker
        String mapperTemplatePath = "/templates/mapper.xml.ftl";
        String dtoTemplatePath = "/code_tpl/dto.java.ftl";
        String voTemplatePath = "/code_tpl/vo.java.ftl";
        String pojoMapperTemplatePath = "/code_tpl/pojoMapper.java.ftl";
        // ?????????????????????
        List<FileOutConfig> focList = new ArrayList<>();
        // ?????????????????????????????????,?????????mapper??????
        focList.add(new FileOutConfig(mapperTemplatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {

                return projectPath + "/src/main/resources/mapper/" + pc.getModuleName()
                        + "/" + tableInfo.getMapperName() + StringPool.DOT_XML;
            }
        });
        //??????DTO
        focList.add(new FileOutConfig(dtoTemplatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                String className = tableInfo.getEntityName().replace("Entity", "DTO");
                return projectPath + "/src/main/java/" +
                        destPackage.replace(".", "/") + "/" + pc.getModuleName()
                        + "/dto/" + className
                        + StringPool.DOT_JAVA;
            }
        });
        //??????VO
        focList.add(new FileOutConfig(voTemplatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                String className = tableInfo.getEntityName().replace("Entity", "VO");
                return projectPath + "/src/main/java/" +
                        destPackage.replace(".", "/") + "/" + pc.getModuleName()
                        + "/vo/" + className
                        + StringPool.DOT_JAVA;
            }
        });
        //??????POJO Mapper
        focList.add(new FileOutConfig(pojoMapperTemplatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                String className = tableInfo.getEntityName().replace("Entity", "PojoMapper");
                return projectPath + "/src/main/java/" +
                        destPackage.replace(".", "/") + "/" + pc.getModuleName()
                        + "/pmapper/" + className
                        + StringPool.DOT_JAVA;
            }
        });
        cfg.setFileOutConfigList(focList);
        return cfg;
    }

    public void gen(String moduleName, String classDesc, String... tables) {
        //?????????
        DataSourceConfig dsc = initDsc();
        //????????????
        GlobalConfig gc = initGc(moduleName);
        // ?????????
        PackageConfig pc = new PackageConfig();
        //????????????????????????????????????
        pc.setModuleName(moduleName)
                //??????????????????
                .setParent(destPackage);
        //????????????
        InjectionConfig cfg = initIc(pc, classDesc);
        // ????????????
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setXml(null)
                .setController("/code_tpl/ordController.java")
//                .setController("/code_tpl/controller.java")
                .setEntity("/code_tpl/entity.java")
                .setService("/code_tpl/service.java")
                .setServiceImpl("/code_tpl/serviceImpl.java")
                .setMapper("/code_tpl/mapper.java");
        // ????????????
        StrategyConfig strategy = initSc(tables);
        //????????????
        AutoGenerator mpg = new AutoGenerator();
        mpg.setPackageInfo(pc)
                .setGlobalConfig(gc)
                .setDataSource(dsc)
                .setCfg(cfg).setTemplate(templateConfig)
                .setStrategy(strategy)
                //??????freemarker??????
                .setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }
}
