package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Item;
import com.example.demo.repository.ItemRepository;
import com.example.demo.dto.ItemSearchRequestDto; // DTO 임포트

import java.util.Optional;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping
    public Page<Item> getAllItems(
            ItemSearchRequestDto searchDto, // DTO로 변경
            Pageable pageable) {

        if (searchDto.getName() != null || searchDto.getDescription() != null) {
            // name 또는 description 파라미터가 있으면 필터링
            String searchName = Optional.ofNullable(searchDto.getName()).orElse("");
            String searchDescription = Optional.ofNullable(searchDto.getDescription()).orElse("");
            return itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    searchName, searchDescription, pageable);
        } else {
            // 파라미터가 없으면 모든 아이템 반환
            return itemRepository.findAll(pageable);
        }
    }

    @GetMapping("/{id}")
    public Item getItemById(@PathVariable Long id) {
        return itemRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Item createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }

    @PutMapping("/{id}")
    public Item updateItem(@PathVariable Long id, @RequestBody Item itemDetails) {
        Item item = itemRepository.findById(id).orElse(null);
        if (item != null) {
            item.setName(itemDetails.getName());
            item.setDescription(itemDetails.getDescription());
            return itemRepository.save(item);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        itemRepository.deleteById(id);
    }
}