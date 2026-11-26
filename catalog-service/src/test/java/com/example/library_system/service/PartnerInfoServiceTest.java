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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerInfoServiceTest {

    private static final String KOBZAR = "978-966-01-0001-1";

    @Mock
    private BookService bookService;

    @Mock
    private RatingLookup ratingLookup;

    @Mock
    private OfferLookup offerLookup;

    @Mock
    private DiscountLookup discountLookup;

    @InjectMocks
    private PartnerInfoService partnerInfoService;

    @Test
    void purchaseQuote_appliesPartnerDiscountToPartnerPrice() {
        when(bookService.findById(1L)).thenReturn(book(KOBZAR));
        when(offerLookup.findOffer(KOBZAR)).thenReturn(new StoreOffer(new BigDecimal("420.00"), true, true));
        when(discountLookup.findDiscount(KOBZAR)).thenReturn(new BookDiscount(KOBZAR, 10));

        PurchaseQuoteResponse quote = partnerInfoService.purchaseQuote(1L);

        assertThat(quote.partnerPriceUah()).isEqualByComparingTo("420.00");
        assertThat(quote.discountPercent()).isEqualTo(10);
        assertThat(quote.priceAfterDiscountUah()).isEqualByComparingTo("378.00");
        assertThat(quote.inStock()).isTrue();
    }

    @Test
    void partnerRating_returnsRatingForBookIsbn() {
        when(bookService.findById(1L)).thenReturn(book(KOBZAR));
        when(ratingLookup.findRating(KOBZAR)).thenReturn(new BookRating(4.8, 312, true));

        PartnerRatingResponse rating = partnerInfoService.partnerRating(1L);

        assertThat(rating.isbn()).isEqualTo(KOBZAR);
        assertThat(rating.average()).isEqualTo(4.8);
        assertThat(rating.reviewsCount()).isEqualTo(312);
    }

    @Test
    void bookWithoutIsbn_notFoundWithoutAskingPartner() {
        when(bookService.findById(5L)).thenReturn(book(null));

        assertThrows(PartnerDataNotFoundException.class, () -> partnerInfoService.purchaseQuote(5L));

        verifyNoInteractions(ratingLookup, offerLookup, discountLookup);
    }

    private static BookDto book(String isbn) {
        BookDto book = new BookDto();
        book.setId(1L);
        book.setTitle("Кобзар");
        book.setIsbn(isbn);
        return book;
    }
}