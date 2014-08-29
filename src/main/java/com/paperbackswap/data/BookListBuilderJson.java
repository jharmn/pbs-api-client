package com.paperbackswap.data;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.paperbackswap.Url.PbsUrlBuilder;
import com.paperbackswap.exceptions.BookListBuilderException;
import com.paperbackswap.exceptions.BooksResponseHasErrorsException;
import com.paperbackswap.exceptions.InvalidBookException;
import com.paperbackswap.exceptions.InvalidBooksResponseException;
import com.paperbackswap.modules.BookModule;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a list of books from PBS API
 * Supports list RequestTypes, as well as detail/single book RequestType
 * @see com.paperbackswap.Url.PbsUrl
 */
public class BookListBuilderJson implements BookListBuilder {
	protected static Injector mInjector;

	@Inject
	public BookListBuilderJson() {
		mInjector = Guice.createInjector(new BookModule());
	}

	public BookList construct(JSONObject source)
            throws BookListBuilderException, InvalidBookException, InvalidBooksResponseException, BooksResponseHasErrorsException {
		return fromResponse(source);
	}

	/***
	 * Parses book responses into a list of JSON objects
	 * 
	 * @param response JSON object with PBS response including books
	 * @return List of books
	 * @throws BookListBuilderException
	 */
	protected BookList fromResponse(JSONObject response)
            throws BookListBuilderException, InvalidBookException, InvalidBooksResponseException, BooksResponseHasErrorsException {

        JSONObject responseObject = response.optJSONObject("Response");
        if (responseObject == null) {
            throw new InvalidBooksResponseException("Response object was invalid");
        }
        if (responseObject.has("error")) {
            throw new BooksResponseHasErrorsException(String.format("Error:%s", responseObject.getString("error")));
        }

        return new BookList(getListOrSingle(responseObject), getNextPage(response));
	}

    /**
     * Builds a list of all books, or a list with one book if a single is present
     * @param responseObject Expects "Response" object from API response object
     * @return List of Book objects
     * @throws InvalidBooksResponseException
     * @throws BookListBuilderException
     * @throws InvalidBookException
     * @see com.paperbackswap.data.Book
     */
    protected List<Book> getListOrSingle(JSONObject responseObject) throws InvalidBooksResponseException, BookListBuilderException, InvalidBookException {
        List<Book> bookList = new ArrayList<Book>();
        JSONArray booksArr = getBooksArray(responseObject);

        // Tries to get array of books, falls back to single
        if (booksArr != null) {
            // Process as an array
            for (int i = 0; i < booksArr.length(); i++) {
                JSONObject book = booksArr.optJSONObject(i);
                if (book != null) {
                    bookList.add(mInjector.getInstance(BookBuilder.class)
                            .construct(book));
                } else {
                    throw new InvalidBooksResponseException(
                            String.format("Book at position %d was invalid", i));
                }
            }
        } else {
            // Process as single book
            JSONObject book = getBookObject(responseObject);
            if (book != null) {
                bookList.add(mInjector.getInstance(BookBuilder.class)
                        .construct(book));
            } else {
                throw new InvalidBooksResponseException("No books were found in the response");
            }
        }
        return bookList;
    }


    /**
     * Builds a list of books from response/books
     * @param responseObject Expects "Response" object from API response object
     * @return The JSON array full of books. Could be null if this is a single book response.
     * @throws BookListBuilderException
     * @throws InvalidBooksResponseException
     */
    protected JSONArray getBooksArray(JSONObject responseObject)
            throws BookListBuilderException, InvalidBooksResponseException {
        JSONObject books = getBooksObject(responseObject);
        Object book = books.opt("Book");
        if (!(book instanceof JSONArray)) {
            throw new InvalidBooksResponseException("Book list was invalid");
        }
        return books.optJSONArray("Book");
    }

    /**
     *
     * @param responseObject Expects "Response" object from API response object
     * @return The "Books" node in the response object
     * @throws BookListBuilderException
     * @throws InvalidBooksResponseException
     */
    protected JSONObject getBooksObject(JSONObject responseObject)
            throws BookListBuilderException, InvalidBooksResponseException {
        return responseObject.optJSONObject("Books");
    }

    /**
     * Retrieves single book from response if no list was provided
     * @param responseObject Expects "Response" object from API response object
     * @return The "Book" node in the response object
     * @throws BookListBuilderException
     * @throws InvalidBooksResponseException
     */
	protected JSONObject getBookObject(JSONObject responseObject) throws BookListBuilderException, InvalidBooksResponseException {
		JSONObject books = getBooksObject(responseObject);
		return books.optJSONObject("Book");
	}

    /**
     * Retrieves URL for next list of results in paged set, if it exists
     * @param responseObject Expects "Response" object from API response object
     * @return URL for next list of results
     * @throws BookListBuilderException
     * @throws InvalidBooksResponseException
     * @throws BooksResponseHasErrorsException
     * @see com.paperbackswap.Url.PbsUrlBuilder
     */
	protected PbsUrlBuilder getNextPage(JSONObject responseObject) throws BookListBuilderException, InvalidBooksResponseException, BooksResponseHasErrorsException {
		PbsUrlBuilder next = null;

        if (responseObject.has("ResultsNextSet")) {
            next = PbsUrlBuilder.fromUrl(responseObject.getString("ResultsNextSet"));
        }
    
        return next;
	}
}
