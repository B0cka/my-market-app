package com.b0cka.service;

import com.b0cka.dto.ItemsPageDto;
import com.b0cka.models.Item;

import java.io.IOException;

public interface ItemService {

    ItemsPageDto getItems(String search, String sort, int page, int size);

    byte[] getImg(String path) throws IOException;

    String mainPageAction(Long id, String action, String search, String sort, int pageNumber, int pageSize);

    Item getItem(Long id);

    String itemPageAction(Long id, String action);
}
