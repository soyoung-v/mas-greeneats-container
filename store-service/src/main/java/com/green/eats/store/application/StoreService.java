package com.green.eats.store.application;

import com.green.eats.store.entity.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreService {
    private final MenuRepository menuRepository;

    public List<Menu> getAllMenus(){
        return menuRepository.findAll();
    }
}
