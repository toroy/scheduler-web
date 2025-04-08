package com.clubfactory.platform.scheduler.web.server.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import com.clubfactory.platform.scheduler.dal.enums.IEnum;

public class StringToEnumConverterFactory implements ConverterFactory<String, IEnum> {

    @Override
    public <T extends IEnum> Converter<String, T> getConverter(Class<T> aClass) {
        return new StringToEnumConverter<>(aClass);
    }

    //将字符串转换成枚举来
    private final class StringToEnumConverter<T extends IEnum> implements Converter<String, T> {

        private Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String s) {
//            for (T t : enumType.getEnumConstants()) {
//                if (t.getByName().equals(s)) {
//                    return t;
//                }
//            }
            return null;
        }
    }

}
