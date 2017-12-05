/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hantsy.ee8sample.rest;

import com.github.hantsy.ee8sample.domain.Favorite;
import com.github.hantsy.ee8sample.domain.Slug;
import com.github.hantsy.ee8sample.domain.Username;
import com.github.hantsy.ee8sample.repository.FavoriteRepository;
import java.util.List;
import static java.util.stream.Collectors.toList;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author hantsy
 */
//@Path("favorites")
@Stateless
public class FavoriteResource {

    @PathParam("slug")
    private String slug;

    @Inject
    FavoriteRepository favorites;

    @Inject
    SecurityContext securityContext;

    @GET
    public Response allFavoritesOfPost() {
        List<String> usernames = favorites.findByPost(slug).stream().map(f -> f.getUser().getUsername()).collect(toList());
        return Response.ok(usernames).build();
    }

    @POST
    public Response favoritePost() {
        return favorites.findBySlugAndUsername(slug, securityContext.getCallerPrincipal().getName())
                .map(
                        f -> {
                            favorites.delete(f);
                            return Response.ok().build();
                        }
                )
                .orElseGet(
                        () -> {
                            favorites.save(Favorite.builder().post(new Slug(slug)).user(new Username(securityContext.getCallerPrincipal().getName())).build());
                            return Response.ok().build();
                        }
                );
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public Response countOfPost() {
        return Response.ok(favorites.countByPost(slug)).build();
    }

    @GET
    @Path("exists")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isFavorited() {
        return Response.ok(favorites.postIsFavorited(slug, securityContext.getCallerPrincipal().getName())).build();
    }

}
