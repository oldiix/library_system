package com.example.library_system.service;

import com.example.common.dto.BookDiscount;
import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import com.example.library_system.dto.BookDto;
import com.example.library_system.dto.PartnerRatingResponse;
import com.example.library_system.dto.PurchaseQuoteResponse;
import com.example.library_system.partner.DiscountLookup;
import com.example.library_system.partner.OfferLookup;
import com.example.library_system.partner.PartnerDataNotFoundException;
import com.example.library_system.partner.RatingLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PartnerInfoService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BookService bookService;
    private final RatingLookup ratingLookup;
    private final OfferLookup offerLookup;
    private final DiscountLookup discountLookup;

    public PartnerRatingResponse partnerRating(Long bookId) {
        BookDto book = bookService.findById(bookId);
        String isbn = requireIsbn(book);
        BookRating rating = ratingLookup.findRating(isbn);
        return new PartnerRatingResponse(book.getId(), book.getTitle(), isbn,
                rating.average(), rating.reviewsCount());
    }

    public PurchaseQuoteResponse purchaseQuote(Long bookId) {
        BookDto book = bookService.findById(bookId);
        String isbn = requireIsbn(book);
        StoreOffer offer = offerLookup.findOffer(isbn);
        BookDiscount discount = discountLookup.findDiscount(isbn);
        BigDecimal finalPrice = offer.priceUah()
                .multiply(BigDecimal.valueOf(100L - discount.percent()))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        return new PurchaseQuoteResponse(book.getId(), book.getTitle(), isbn,
                offer.priceUah(), discount.percent(), finalPrice, offer.inStock());
    }

    private static String requireIsbn(BookDto book) {
        if (book.getIsbn() == null || book.getIsbn().isBlank()) {
            throw new PartnerDataNotFoundException(
                    "Книга «" + book.getTitle() + "» не має ISBN, тому партнер не може її знайти");
        }
        return book.getIsbn().trim();
    }
}