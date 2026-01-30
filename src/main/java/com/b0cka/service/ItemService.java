package com.b0cka.service;

import com.b0cka.dto.ItemsPageDto;

import java.io.IOException;

public interface ItemService {

    ItemsPageDto getItems(String search, String sort, int page, int size);

    byte[] getImg(String path) throws IOException;
}
