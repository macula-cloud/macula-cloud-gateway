package org.macula.cloud.gateway.repository;

import org.macula.cloud.gateway.domain.GatewayRoute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayRouteRepository extends JpaRepository<GatewayRoute, Long> {

}
