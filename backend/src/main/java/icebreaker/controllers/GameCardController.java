package icebreaker.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import icebreaker.models.Category;
import icebreaker.models.GameCard;
import icebreaker.models.User;
import icebreaker.payload.request.gamecard.GameCardAddRequest;
import icebreaker.payload.request.gamecard.GameCardCategoryFilterRequest;
import icebreaker.payload.request.gamecard.GameCardUpdateRequest;
import icebreaker.payload.response.MessageResponse;
import icebreaker.payload.response.gamecard.GameCardResponse;
import icebreaker.repository.CategoryRepository;
import icebreaker.repository.GameCardRepository;
import icebreaker.repository.UserRepository;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/gamecard")
public class GameCardController {

    @Autowired
    GameCardRepository gameCardRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/get/id/{id}")
    public ResponseEntity<?> getGameCardById(@PathVariable long id) {

        GameCard gameCard = gameCardRepository.findById(id).orElse(null);

        if (gameCard == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Finner ikke bli-kjent lek med den ID-en"));
        }

        return ResponseEntity.ok(new GameCardResponse(gameCard));
    }

    @GetMapping("/get/title/{title}")
    public ResponseEntity<?> getGameCardByName(@PathVariable String title) {

        GameCard gameCard = gameCardRepository.findByTitle(title);

        if (gameCard == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Finner ikke bli-kjent lek med den tittelen"));
        }

        return ResponseEntity.ok(new GameCardResponse(gameCard));
    }

    @GetMapping("/get/all")
    public ResponseEntity<?> getGameCard() {
        List<GameCardResponse> response = new ArrayList<>(
                gameCardRepository.findAll().stream().map(GameCardResponse::new).toList());
        response.sort((a, b) -> {
            if (a.getAverageRating() == null && b.getAverageRating() == null) {
                return 0;
            } else if (a.getAverageRating() == null) {
                return 1;
            } else if (b.getAverageRating() == null) {
                return -1;
            } else {
                return Double.compare(b.getAverageRating(), a.getAverageRating());
            }
        });
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get/categories")
    public ResponseEntity<?> filterGameCardByCategory(
            @Valid @RequestBody GameCardCategoryFilterRequest categoryFilterRequest) {

        List<GameCardResponse> response = new ArrayList<>(gameCardRepository
                .findAllByCategoriesIn(categoryFilterRequest.getCategories()).stream()
                .map(GameCardResponse::new).toList());
        response.sort((a, b) -> {
            if (a.getAverageRating() == null && b.getAverageRating() == null) {
                return 0;
            } else if (a.getAverageRating() == null) {
                return 1;
            } else if (b.getAverageRating() == null) {
                return -1;
            } else {
                return Double.compare(b.getAverageRating(), a.getAverageRating());
            }
        });
        return ResponseEntity.ok(response);
    }

    @PutMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addGameCard(@Valid @RequestBody GameCardAddRequest addRequest) {

        String title = addRequest.getTitle();

        if (gameCardRepository.existsByTitle(title)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Bli-kjent lek med den tittelen finnes allerede"));
        }

        Set<Category> categories = categoryRepository.findAllByNameIn(addRequest.getCategories());

        GameCard gameCard = new GameCard(title, addRequest.getRules(), addRequest.getDescription(),
                addRequest.getUsername(), categories);

        gameCardRepository.save(gameCard);

        return ResponseEntity.ok(new MessageResponse("Bli-kjent lek lagt til!"));
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<?> updateGameCard(@Valid @RequestBody GameCardUpdateRequest updateRequest) {

        long id = updateRequest.getId();
        String title = updateRequest.getTitle();

        GameCard gameCard = gameCardRepository.findById(id).orElse(null);

        if (gameCard == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Finner ikke bli-kjent lek med den ID-en"));
        }

        if (gameCardRepository.existsByTitle(title) && !gameCard.getTitle().equals(title)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Bli-kjent lek med den tittelen finnes allerede"));
        }

        gameCard.setTitle(title);
        gameCard.setRules(updateRequest.getRules());
        gameCard.setDescription(updateRequest.getDescription());
        gameCard.setCategories(categoryRepository.findAllByNameIn(updateRequest.getCategories()));
        gameCardRepository.save(gameCard);

        return ResponseEntity.ok(new MessageResponse("Bli-kjent lek oppdatert!"));
    }

    @DeleteMapping("/delete/id/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<?> deleteGameCardById(@PathVariable long id) {

        GameCard gameCard = gameCardRepository.findById(id).orElse(null);

        if (gameCard == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Finner ikke bli-kjent lek med den ID-en"));
        }

        // Remove gameCard from all users' favorites and queue
        for (User user : userRepository.findAll()) {
            user.removeGameCardFromFavorites(gameCard);
            user.removeGameCardFromQueue(gameCard);
            userRepository.save(user); 
        }

        gameCardRepository.delete(gameCard);

        return ResponseEntity.ok(new MessageResponse("Bli-kjent lek slettet!"));
    }

    @DeleteMapping("/delete/title/{title}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<?> deleteGameCardByTitle(@PathVariable String title) {

        GameCard gameCard = gameCardRepository.findByTitle(title);

        if (gameCard == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Finner ikke bli-kjent lek med den tittelen"));
        }

        // Remove gameCard from all users' favorites and queue
        for (User user : userRepository.findAll()) {
            user.removeGameCardFromFavorites(gameCard);
            user.removeGameCardFromQueue(gameCard);
            userRepository.save(user); 
        }

        gameCardRepository.delete(gameCard);

        return ResponseEntity.ok(new MessageResponse("Bli-kjent lek slettet!"));
    }
}
