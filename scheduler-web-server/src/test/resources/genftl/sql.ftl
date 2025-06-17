CREATE TABLE ${tableName}
(
     id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '自增id',
    is_deleted Boolean COMMENT '是否删除，1：是，0：不是',
[#list fieldList as field]
    [#if field.fieldType=='Boolean']
    ${field.fieldDb} Boolean COMMENT '${field.fieldCommet!}',
  [/#if]
  [#if field.fieldType=='String']
    ${field.fieldDb} VARCHAR(256) COMMENT '${field.fieldCommet!}',
  [/#if]
  [#if field.fieldType=='Long']
   ${field.fieldDb} BIGINT COMMENT '${field.fieldCommet!}',
  [/#if]
  [#if field.fieldType=='Integer']
    ${field.fieldDb} INT COMMENT '${field.fieldCommet!}',
  [/#if]
  [#if field.fieldType=='Byte']
    ${field.fieldDb} smallint COMMENT '${field.fieldCommet!}',
  [/#if]
  [#if field.fieldType=='Enum']
    ${field.fieldDb} VARCHAR(8) COMMENT '${field.fieldCommet!}',
  [/#if]
  [#if field.fieldType=='Date']
    ${field.fieldDb} timestamp COMMENT '${field.fieldCommet!}',
  [/#if]
  [#if field.fieldType=='BigDecimal']
    ${field.fieldDb} decimal(10,2) COMMENT '${field.fieldCommet!}',
   [/#if]
 [/#list]
    created_user bigint  COMMENT '创建人',
    updated_user bigint  COMMENT '修改人',
    created_at timestamp null COMMENT '创建时间',
    updated_at timestamp null COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;


