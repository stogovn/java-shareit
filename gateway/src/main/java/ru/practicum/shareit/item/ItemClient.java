package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getAllItemsForUser(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getItemById(Long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> create(Long userId, ItemRequestDto itemDto) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> update(Long userId, Long id, ItemRequestDto itemDto) {
        return patch("/" + id, userId, itemDto);
    }

    public ResponseEntity<Object> searchItems(String text) {
        Map<String, Object> parameters = Map.of(
                "text", text);
        return get("/search?text={text}", parameters);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentRequestDto commentDto) {
        String path = UriComponentsBuilder.fromPath("/{itemId}/comment")
                .buildAndExpand(itemId)
                .toUriString();
        return post(path, userId, commentDto);
    }
}
