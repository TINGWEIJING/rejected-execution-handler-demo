package com.tingcode.RejectedExecutionHandlerDemo.payload;

import lombok.Data;

import java.util.List;

@Data
public class PersonalizedProductCategories {
    private List<String> categories = List.of("Electronics", "Clothing", "Books", "Home Appliances");
}
