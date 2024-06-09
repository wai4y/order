package com.test.order.controller;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.Distance;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixElementStatus;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.LatLng;
import com.test.order.common.IDUtils;
import com.test.order.common.TestContainerConfig;
import com.test.order.controller.dto.OrderStatus;
import com.test.order.model.OrderEntity;
import com.test.order.repository.OrderRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class OrderControllerIntegrationTest extends TestContainerConfig {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GeoApiContext geoApiContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @AfterEach
    public void cleanUp() {
        jdbcTemplate.execute("TRUNCATE TABLE `order`");
    }

    @Test
    void placeOrderSuccess() throws Exception {
        // given
        String request = """
                {
                    "origin": ["22.28383", "114.17312"],
                    "destination": ["22.29795", "114.17216"]
                }
                """;
        DistanceMatrixRow[] rows = buildDistanceMatrixRow(1000L, DistanceMatrixElementStatus.OK);
        DistanceMatrix distanceMatrix = new DistanceMatrix(new String[]{}, new String[]{}, rows);
        DistanceMatrixApiRequest distanceMatrixApiRequest = mock(DistanceMatrixApiRequest.class);

        try (MockedStatic<DistanceMatrixApi> mocked = mockStatic(DistanceMatrixApi.class)) {
            // when
            mocked.when(() -> DistanceMatrixApi.newRequest(geoApiContext))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest
                    .origins(any(LatLng.class)))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest
                    .destinations(any(LatLng.class)))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest.awaitIgnoreError()).thenReturn(distanceMatrix);

            // then
            mvc.perform(post("/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                    ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.distance").value(1000))
                    .andExpect(jsonPath("$.status").value("UNASSIGNED"));
        }

    }


    @ParameterizedTest(name = "Place order failed when request invalid: {0}")
    @MethodSource("invalidRequest")
    void placeOrderFailedWhenRequestInvalid(String request) throws Exception {
        mvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ).andExpect(status().isBadRequest());
    }

    static Stream<Arguments> invalidRequest() {
        return Stream.of(
                Arguments.of(
                        """
                                {
                                    "origin": ["abc", "114.17312"],
                                    "destination": ["22.29795", "114.17216"]
                                }
                                """
                ),
                Arguments.of(
                        """
                                {
                                    "origin": ["23.22", "114.17312"],
                                    "destination": ["abc", "114.17216"]
                                }
                                """
                ),
                Arguments.of(
                        """
                                {
                                    "origin": ["100", "114.17312"],
                                    "destination": ["abc", "114.17216"]
                                }
                                """
                ),
                Arguments.of(
                        """
                                {
                                    "origin": ["23.2323", "2323"],
                                    "destination": ["abc", "114.17216"]
                                }
                                """
                ),
                Arguments.of(
                        """
                                {
                                    "origin": ["23.2323", "114.23"],
                                    "destination": ["23.232", "180.1"]
                                }
                                """
                ),
                Arguments.of(
                        """
                                {
                                    "origin": ["23.2323", "114.23"],
                                    "destination": ["-90.1", "180"]
                                }
                                """
                ),
                Arguments.of(
                        """
                                {
                                    "origin": [],
                                    "destination": ["-90.1", "180"]
                                }
                                """
                )
        );
    }

    private DistanceMatrixRow @NotNull [] buildDistanceMatrixRow(Long distance, DistanceMatrixElementStatus status) {
        DistanceMatrixRow[] rows = new DistanceMatrixRow[1];
        var row = new DistanceMatrixRow();

        DistanceMatrixElement[] elements = new DistanceMatrixElement[1];
        var element = new DistanceMatrixElement();
        element.status = status;
        element.distance = new Distance();
        element.distance.inMeters = distance;
        elements[0] = element;
        row.elements = elements;
        rows[0] = row;
        return rows;
    }


    /*************************************************************************************************/

    @Test
    void takeOrderSuccess() throws Exception {
        var request = """
                {
                    "status": "TAKEN"
                }
                """;
        // add data
        var orderEntity = new OrderEntity();
        orderEntity.setStatus(OrderStatus.UNASSIGNED.name());
        orderEntity.setDistance(1000);
        OrderEntity save = orderRepository.save(orderEntity);

        mvc.perform(patch("/order/" + save.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ).andExpect(status().isOk());
    }


    @Test
    @DisplayName("Take order failed when request invalid")
    void takeOrderFailedWhenRequestInvalid() throws Exception {
        var request = """
                {
                    "status": "xxx"
                }
                """;
        mvc.perform(patch("/order/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ).andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Take order failed when request missing")
    void takeOrderFailedWhenRequestMissing() throws Exception {
        var request = """
                {
                }
                """;
        mvc.perform(patch("/order/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ).andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Take order failed when id invalid")
    void takeOrderFailedWhenIdInvalid() throws Exception {
        var request = """
                {
                    "status": "TAKEN"
                }
                """;
        mvc.perform(patch("/order/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ).andExpect(status().isBadRequest());
    }


    /*************************************************************************************************/


    @Test
    void getOrderSuccess() throws Exception {
        addOrder(10);
        mvc.perform(get("/order" )
                        .param("page", "1")
                        .param("limit", "10")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].distance").value(10))
                .andExpect(jsonPath("$[1].distance").value(11))
                .andExpect(jsonPath("$[9].distance").value(19));
    }

    @Test
    @DisplayName("Get order success with pagination")
    void getOrderSuccessWithPagination() throws Exception {
        addOrder(10);
        mvc.perform(get("/order" )
                        .param("page", "2")
                        .param("limit", "2")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].distance").value(12))
                .andExpect(jsonPath("$[1].distance").value(13));
    }


    @Test
    @DisplayName("Get order success with pagination return empty")
    void getOrderSuccessWithPaginationEmptyResult() throws Exception {
        addOrder(10);
        mvc.perform(get("/order" )
                        .param("page", "6")
                        .param("limit", "2")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    @DisplayName("Get order failed when page is invalid")
    void getOrderFailedWhenPageIsInvalid() throws Exception {
        mvc.perform(get("/order" )
                        .param("page", "0")
                        .param("limit", "10")
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get order failed when limit is invalid")
    void getOrderFailedWhenLimitIsInvalid() throws Exception {
        mvc.perform(get("/order" )
                .param("page", "2")
                .param("limit", "abc")
        ).andExpect(status().isBadRequest());
    }


    private void addOrder(int n) {
        for (int i=0; i<n; i++) {
            var localDateTime = LocalDateTime.of(2024, 6, 9, 12, 0, i);
            String sql = "insert into `order` (id, distance, status, created_at) values (?, ?, ?, ?)";
            jdbcTemplate.update(sql, IDUtils.longUUID(), n + i, OrderStatus.UNASSIGNED.name(), localDateTime);
        }
    }

}
