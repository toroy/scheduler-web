<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="${packagePath}.dal.dao.${simpleClassName}Mapper" >
  <resultMap id="BaseResultMap" type="${packagePath}.dal.po.${simpleClassName}" >
    <id column="id" property="id" />
    <result column="is_deleted" property="isDeleted" />
    <result column="created_at" property="createdAt" />
    <result column="updated_at" property="updatedAt" />
    <result column="created_user" property="createdUser" />
    <result column="updated_user" property="updatedUser" />
[#list fieldList as field]
[#if field.fieldType=='Enum']
    <result column="${field.fieldDb}" property="${field.fieldName}" typeHandler="${packagePath}.dal.base.IntegerAbleEnumHandler"/>
[#else]
    <result column="${field.fieldDb}" property="${field.fieldName}" />
[/#if]
[/#list]
  </resultMap>


  <sql id="Select_Column" >
   id
   ,is_deleted
   ,created_user
   ,updated_user
    [#list fieldList as field]
	,${field.fieldDb}
	[/#list]
  </sql>


  <sql id="Insert_Column">
     created_at
     ,updated_at
     , is_deleted
     ,created_user
     ,updated_user
     [#list fieldList as field]
      ,${field.fieldDb}
     [/#list]
  </sql>

[#noparse]
<sql id="Insert_Value">
  #{po.createdAt}
  ,#{po.updatedAt}
  ,#{po.isDeleted}
  ,#{po.createdUser}
  ,#{po.updatedUser}
 [/#noparse]
 [#list fieldList as field]
    [#noparse],#{po.[/#noparse]${field.fieldName}[#noparse]}[/#noparse]
 [/#list]
</sql>


[#noparse]
 <sql id="Update_Sql" >
    <if test="isDeleted != null" >
    ${joinStr} is_deleted = #{isDeleted}
    </if>
     <if test="createdAt != null" >
    ${joinStr} created_at = #{createdAt}
     </if>
     <if test="createdUser != null" >
    ${joinStr} created_user = #{createdUser}
     </if>
[/#noparse]
[#list fieldList as field]
[#if field.fieldType=='String']
     <if test="${field.fieldName} != null and ${field.fieldName} != ''" >
        [#noparse]${joinStr}[/#noparse] ${field.fieldDb} [#noparse]=#{[/#noparse]${field.fieldName}[#noparse]}[/#noparse]
     </if>
[#else]
     <if test="${field.fieldName} != null" >
         [#noparse]${joinStr}[/#noparse] ${field.fieldDb} [#noparse]=#{[/#noparse]${field.fieldName}[#noparse]}[/#noparse]
     </if>
[/#if]
[/#list]
</sql>

<sql id="Table_Name" >
   ${tableName}
</sql>


[#noparse]
      <sql id="Where_Sql" >
      	<if test="id != null" >
      		and id = #{id}
      	</if>
        <if test="ids != null and ids.size() > 0" >
          and
          <choose>
            <when test="queryListFieldName != null  and queryListFieldName !=''">${queryListFieldName}</when>
            <otherwise>id</otherwise>
          </choose>
          in
          <foreach collection="ids" item="item" open="(" separator="," close=")">
              #{item}
          </foreach>
        </if>
        <include refid="Update_Sql"><property name="joinStr" value="and"/></include>
    </sql>


    <insert id="save" useGeneratedKeys="true" keyProperty="id">
        insert into
        <include refid="Table_Name"/>
        (
        <include refid="Insert_Column"/>
        )
        values (
        <include refid="Insert_Value"/>
        )
    </insert>


    <insert id="saveBatch">
        insert into
        <include refid="Table_Name"/>
        (
        <include refid="Insert_Column"/>
        )
        values
        <foreach item="po" collection="list" separator=",">
        (
          <include refid="Insert_Value"/>
        )
        </foreach>
    </insert>


  <select id="get" resultMap="BaseResultMap">
    select
    <include refid="Select_Column" />
    from <include refid="Table_Name" />
    where 1 = 1
    <include refid="Where_Sql"></include>
  </select>


  <select id="list" resultMap="BaseResultMap" >
    select
    <include refid="Select_Column" />
    from <include refid="Table_Name" />
    where 1 = 1
    <include refid="Where_Sql"></include>
  </select>


  <select id="count" resultType="java.lang.Integer">
      select count(1) from <include refid="Table_Name" />
      where 1 = 1
      <include refid="Where_Sql"></include>
   </select>


  <delete id="remove">
      delete from <include refid="Table_Name" />
      where 1 = 1
      <include refid="Where_Sql"></include>
  </delete>


  <update id="edit">
    update <include refid="Table_Name" />
    <set >
       updated_at = #{updatedAt}
      <include refid="Update_Sql"><property name="joinStr" value=","/></include>
    </set>
    where 1 = 1
    <if test="otherParam != null" >
      <foreach collection="otherParam" index="k" item="v">
        <if test="null != v">
          and ${k} = #{v}
        </if>
      </foreach>
    </if>
  </update>

  <update id="logicRemove">
      update <include refid="Table_Name" />
      <set >
         updated_at = #{updatedAt},is_deleted = 1
      </set>
      where 1 = 1
      <include refid="Where_Sql"></include>
   </update>
[/#noparse]


</mapper>