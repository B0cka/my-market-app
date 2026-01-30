package com.b0cka.service.impl;

import com.b0cka.repository.ItemRepository;
import com.b0cka.dto.ItemsPageDto;
import com.b0cka.models.Item;
import com.b0cka.models.Paging;
import com.b0cka.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CartService cartService;
    @Override
    public ItemsPageDto getItems(String search, String sort, int page, int size) {
        Sort sortObj = Sort.unsorted();

        if ("ALPHA".equals(sort)) {
            sortObj = Sort.by("title").ascending();
        } else if ("PRICE".equals(sort)) {
            sortObj = Sort.by("price").ascending();
        }

        Pageable pageable = PageRequest.of(page - 1, size, sortObj);
        Page<Item> pageResult;

        if (search != null && !search.isBlank()) {
            pageResult = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        } else {
            pageResult = itemRepository.findAll(pageable);
        }

        List<Item> items = pageResult.getContent();
        List<List<Item>> ret = new ArrayList<>();
        List<Item> row = new ArrayList<>();

        for(Item item : items){
            int countInCart = cartService.getCount(item.getId());
            item.setCount(countInCart);

            row.add(item);
            if(row.size() == 3){
                ret.add(row);
                row = new ArrayList<>();
            }

        }

        if(!row.isEmpty()){
            while (row.size() != 3){
                Item dummy = new Item();
                dummy.setId(-1L);
                row.add(dummy);
            }
            ret.add(row);
        }

        Paging paging = Paging.builder()
                .pageNumber(pageResult.getNumber() + 1)
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .pageSize(pageResult.getSize())
                .build();

        return new ItemsPageDto(ret, paging);
    }

    @Override
    public byte[] getImg(String filename) {
        String path = "/images/" + filename;
        try {
            var inputStream = getClass().getResourceAsStream(path);
            if (inputStream == null) {
                return new byte[0];
            }
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String mainPageAction(Long id, String action, String search, String sort, int pageNumber, int pageSize) {

        if ("PLUS".equals(action)) {

            cartService.add(id);
        } else if ("MINUS".equals(action)) {
            cartService.remove(id);
        }

        String redirectUrl = "/items" +
                "?search=" + (search != null ? search : "") +
                "&sort=" + sort +
                "&pageNumber=" + pageNumber +
                "&pageSize=" + pageSize;

        return "redirect:" + redirectUrl;
    }
}
