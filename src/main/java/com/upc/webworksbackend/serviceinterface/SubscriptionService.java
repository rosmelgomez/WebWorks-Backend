package com.upc.webworksbackend.serviceinterface;

import com.upc.webworksbackend.dto.SubscriptionDbo;
import com.upc.webworksbackend.dtoaux.SubscriptionCheck;
import com.upc.webworksbackend.dtoaux.SubscriptionSummaryDto;
import com.upc.webworksbackend.model.*;
import com.upc.webworksbackend.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

final SubscriptionRepository subscriptionRepository;
final PlanRepository planRepository;
final UserRepository userRepository;
final MethodPaymentRepository methodPaymentRepository;
final PromotionCodeRepository promotionCodeRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, PlanRepository planRepository, UserRepository userRepository, MethodPaymentRepository methodPaymentRepository, PromotionCodeRepository promotionCodeRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.methodPaymentRepository = methodPaymentRepository;
        this.promotionCodeRepository = promotionCodeRepository;
    }

    public Boolean addSubscription(SubscriptionDbo subscriptionDbo) {
        // Obtención de las entidades relacionadas
        PlanModel planModel = planRepository.findById(subscriptionDbo.getId_plan()).orElse(null);
        UserModel userModel = userRepository.findById(subscriptionDbo.getId_user()).orElse(null);
        MethodPaymentModel methodPaymentModel = subscriptionDbo.getId_methodPayment() == null || subscriptionDbo.getId_methodPayment() <= 0
                ? null : methodPaymentRepository.findById(subscriptionDbo.getId_methodPayment()).orElse(null);
        PromotionCodeModel promotionCodeModel = subscriptionDbo.getId_promotionCode() == null || subscriptionDbo.getId_promotionCode() <= 0
                ? null : promotionCodeRepository.findById(subscriptionDbo.getId_promotionCode()).orElse(null);

        // Validaciones
        if (planModel == null || userModel == null || subscriptionDbo.getDateStart() == null
                || subscriptionDbo.getDateEnd() == null
                || !subscriptionDbo.getDateEnd().after(subscriptionDbo.getDateStart())) {
            return false;
        }

        if (planModel.getPrice() > 0 && (methodPaymentModel == null
                || !methodPaymentModel.getUserMethodPayment().getId().equals(userModel.getId()))) {
            return false;
        }

        // Mapeo y asignación de relaciones
        ModelMapper modelMapper = new ModelMapper();
        SubscriptionModel subscriptionModel = modelMapper.map(subscriptionDbo, SubscriptionModel.class);
        // el frontend envia id 0: forzar alta nueva en lugar de merge de un registro inexistente
        subscriptionModel.setId(null);

        subscriptionModel.setPlanSubscription(planModel);
        subscriptionModel.setUserSubscription(userModel);
        if (methodPaymentModel != null) {
            subscriptionModel.setMethodPaymentSubscription(methodPaymentModel);
        }
        if (promotionCodeModel != null) {
            subscriptionModel.setSubscriptionPromotionCode(promotionCodeModel);
        }

        // Guardar la suscripción
        subscriptionRepository.save(subscriptionModel);
        return true;
    }

    public SubscriptionCheck SubscriptionActive(Integer id){
        List<SubscriptionModel> check=subscriptionRepository.findActiveByUser(id, new Date());
        SubscriptionCheck subscriptionCheck=new SubscriptionCheck();
        if(!check.isEmpty()) {
            SubscriptionModel subscriptionModel = check.getFirst();
            subscriptionCheck.setStatus(true);
            subscriptionCheck.setAmount(subscriptionModel.getAmountTotal());
            subscriptionCheck.setMaxNumberProject(subscriptionModel.getPlanSubscription().getMaxNumberProject());
            subscriptionCheck.setMaxNumberRepository(subscriptionModel.getPlanSubscription().getMaxNumberRepository());
            subscriptionCheck.setPlanName(subscriptionModel.getPlanSubscription().getName());
        }
        return subscriptionCheck;
    }

    public Optional<SubscriptionModel> activeSubscription(Integer userId) {
        return subscriptionRepository.findActiveByUser(userId, new Date()).stream().findFirst();
    }

    public List<SubscriptionSummaryDto> listSubscriptionsByUser(Integer id) {
        List<SubscriptionModel> subscriptions = subscriptionRepository.findSubscriptionModelByUserSubscription_Id(id);
        List<SubscriptionSummaryDto> subscriptionSummaryDtos = new ArrayList<>();
        if(!subscriptions.isEmpty()){
            for (SubscriptionModel subscription : subscriptions) {
                SubscriptionSummaryDto subscriptionSummaryDto = new SubscriptionSummaryDto();

                if (subscription.getSubscriptionPromotionCode()!=null) {
                    PromotionCodeModel promotionCodeModel =promotionCodeRepository.findById(subscription.getSubscriptionPromotionCode().getId()).orElse(null);
                    if (promotionCodeModel != null) {
                        subscriptionSummaryDto.setNamePromotionCode(promotionCodeModel.getCode());
                        subscriptionSummaryDto.setDiscountPercentage(promotionCodeModel.getDiscountPercentage());
                    }
                }
                if (subscription.getPlanSubscription() != null) {
                    subscriptionSummaryDto.setDateStart(subscription.getDateStart());
                    subscriptionSummaryDto.setDateEnd(subscription.getDateEnd());
                    subscriptionSummaryDto.setAmountTotal(subscription.getAmountTotal());
                    subscriptionSummaryDto.setNamePlan(subscription.getPlanSubscription().getName());
                }
                if(subscription.getMethodPaymentSubscription() != null){
                    subscriptionSummaryDto.setNumberMethodPayment(subscription.getMethodPaymentSubscription().getNumberCard());
                }else{
                    subscriptionSummaryDto.setNumberMethodPayment("");
                }
                subscriptionSummaryDtos.add(subscriptionSummaryDto);
            }
        }

        return subscriptionSummaryDtos;
    }

}
