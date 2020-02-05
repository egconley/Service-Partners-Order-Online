package com.rachnicrice.spordering.controllers;

import com.rachnicrice.spordering.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.context.LazyContextVariable;

import java.sql.Timestamp;

import java.security.Principal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Controller
public class OrderController {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ApplicationUserRepository applicationUserRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    LineItemRepository lineItemRepository;


    @GetMapping("/mycart")
    public String showCart(Model model, Principal p, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "id") String sortBy) {

        if (p != null) {
            System.out.println(p.getName()+" is logged in!");
            model.addAttribute("username", p.getName());
        } else {
            System.out.println("nobody is logged in");
        }

        ApplicationUser loggedInUser = applicationUserRepository.findByUsername(p.getName());

        System.out.println("are user orders null? " + loggedInUser.getOrders());


        if (loggedInUser.getOrders().size() > 0) {
            List<Order> userOrders = loggedInUser.getOrders();
            for (Order order : userOrders) {
                if (order.getSubmitted()==false) {
                    Order unsubmittedOrder = order;
                    List<LineItem> lineItems = unsubmittedOrder.getItemsInThisOrder();
                    List<Product> cartProducts = new LinkedList<>();
                    for (LineItem item : lineItems) {
                        Product cartProduct = item.getProduct();
                        cartProducts.add(cartProduct);
                    }
                    model.addAttribute("dataList", lineItems);
                    model.addAttribute("order", unsubmittedOrder);
                }
            }
        } else {
            Order newOrder = new Order(applicationUserRepository.findByUsername(p.getName()), new Date(), false);
            orderRepository.save(newOrder);
            model.addAttribute("order", newOrder);
        }
        model.addAttribute("currentPage",page);

        return "mycart";
    }

    // update to match route in form
    @DeleteMapping("/mycart/delete/{id}")
    public RedirectView deleteLineItem(@PathVariable long id, Principal p) {
        System.out.println("MADE INTO DELETE MAPPING"+ id);
        ApplicationUser loggedInUser = applicationUserRepository.findByUsername(p.getName());
        LineItem lineItem = lineItemRepository.getOne(id);

        ApplicationUser userAssociatedWithLineItem = lineItem.getOrder().getUser();

        if (loggedInUser == userAssociatedWithLineItem) {
//            lineItemRepository.getOne(id);
            System.out.println("made to spot where the delete will happen");

        }

        return new RedirectView("/mycart");
    }

    // update to match route in form
    @PostMapping("/mycart/edit/{id}")
    public RedirectView updateQuantity(@PathVariable long id, Principal p, int quantity) {

        System.out.println("MADE INTO EDIT MAPPING"+ id + " QUANTITY "+ quantity);

        ApplicationUser loggedInUser = applicationUserRepository.findByUsername(p.getName());
        LineItem lineItem = lineItemRepository.getOne(id);

        ApplicationUser userAssociatedWithLineItem = lineItem.getOrder().getUser();

        if (loggedInUser == userAssociatedWithLineItem) {
            lineItem.setQuantity(quantity);
            System.out.println("made to spot where the EDIT will happen");


        }

        return new RedirectView("/mycart");
    }

    @GetMapping("/mycart/findOne")
    @ResponseBody
    public LineItem findOne(Long id) {
        return lineItemRepository.getOne(id);

    }
}
