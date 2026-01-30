package com.b0cka.controllers;

import com.b0cka.dto.ItemsPageDto;
import com.b0cka.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items")
    public String getItems(
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize
    ) {
        log.info("Get запрос: search={}, sort={}, page={}", search, sort, pageNumber);

        ItemsPageDto itemsPageDto = itemService.getItems(search, sort, pageNumber, pageSize);

        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("items", itemsPageDto.getGrid());
        model.addAttribute("paging", itemsPageDto.getPaging());

        return "items";
    }

    @GetMapping("/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<byte[]> getImg(@PathVariable String filename) {
        try {
            byte[] image = itemService.getImg(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(image);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/items")
    public String mainPageAction(
            @RequestParam Long id,
            @RequestParam String action,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize
    ) {
        return itemService.mainPageAction(id, action, search, sort, pageNumber, pageSize);
    }
}