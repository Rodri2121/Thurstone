package gm.thurstone.security;

import gm.thurstone.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Adaptador de {@link Usuario} (psicólogo) al modelo de seguridad de Spring.
 * Expone el {@link Usuario} autenticado para que los controladores lo obtengan
 * vía {@code @AuthenticationPrincipal} sin volver a consultar la base.
 *
 * {@code UserDetails} ya extiende {@link Serializable}; se declara explícito y
 * con {@code serialVersionUID} porque este objeto viaja en el SecurityContext de
 * la sesión, que Spring Session JDBC persiste serializado (su campo {@link Usuario}
 * también es Serializable).
 */
public class UsuarioDetails implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_PSICOLOGO"));
    }

    @Override
    public String getPassword() {
        return usuario.getContrasenaHash();
    }

    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
