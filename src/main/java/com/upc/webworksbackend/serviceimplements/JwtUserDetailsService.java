package com.upc.webworksbackend.serviceimplements;
import com.upc.webworksbackend.model.CompanyModel;
import com.upc.webworksbackend.model.UserModel;
import com.upc.webworksbackend.repository.CompanyRespository;
import com.upc.webworksbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class   JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRespository companyRespository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserModel user= userRepository.findByUsername(username);
        CompanyModel company=companyRespository.findByUsername(username);
        if(user!=null){
            List<GrantedAuthority> grantedAuthorities=new ArrayList<>();
            grantedAuthorities.add(new SimpleGrantedAuthority(user.getRol()));
            return new org.springframework.security.core.userdetails.User(user.getUsername(),user.getPassword(),true,true,true,true,grantedAuthorities);
        }
        if (company!=null){
            List<GrantedAuthority> grantedAuthorities=new ArrayList<>();
            grantedAuthorities.add(new SimpleGrantedAuthority(company.getRol()));
            return new org.springframework.security.core.userdetails.User(company.getUsername(),company.getPassword(),true,true,true,true,grantedAuthorities);
        }
        throw new UsernameNotFoundException("este usuario "+username+" no existe");
    }

}
