package menu;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


//        Создать таблицу «Меню в ресторане». Колонки: название блюда, его стоимость, вес, наличие скидки.
//        Написать код для добавления записей в таблицу и их выборки по критериям «стоимость от-до»,
//        «только со скидкой», выбрать набор блюд так, чтобы их суммарный вес был не более 1 КГ.

public class App {
    static EntityManagerFactory emf;
    static EntityManager em;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            // create connection
            emf = Persistence.createEntityManagerFactory("JPATest");
            em = emf.createEntityManager();
            try {
                while (true) {
                    System.out.println("1: add dish");
                    System.out.println("2: add random dish");
                    System.out.println("3: delete dish");
                    System.out.println("4: change dish");
                    System.out.println("5: view dishes");
                    System.out.println("6: view dishes with discount");
                    System.out.println("7: view dishes with price from _ to _");
                    System.out.println("8: view dishes with weight to 1 kilo");

                    System.out.print("-> ");

                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            addDish(sc);
                            break;
                        case "2":
                            insertRandomDishes(sc);
                            break;
                        case "3":
                            deleteDish(sc);
                            break;
                        case "4":
                            changeDish(sc);
                            break;
                        case "5":
                            viewDishes();
                            break;
                        case "6":
                            viewDishesWithDiscount();
                            break;
                        case "7":
                            viewDishesWithPrice(sc);
                            break;
                        case "8":
                            viewDishesWithWeight(sc);
                            break;
                        default:
                            continue;
                    }
                }
            } finally {
                sc.close();
                em.close();
                emf.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }


    private static void addDish(Scanner sc) {
        System.out.print("Enter dish name: ");
        String name = sc.nextLine();

        System.out.print("Enter dish price: ");
        String sPrice = sc.nextLine();
        double price = Double.parseDouble(sPrice);

        System.out.print("Enter dish weight: ");
        String sWeight = sc.nextLine();
        double weight = Double.parseDouble(sWeight);

        System.out.println("If dish with discount enter 1");
        System.out.print("If dish without discount enter 2: ");
        String sDiscount = sc.nextLine();
        boolean discount = false;
        if ("1".equals(sDiscount))
            discount = true;

        em.getTransaction().begin();
        try {
            Dish d = new Dish(name, price, weight, discount);
            em.persist(d);
            em.getTransaction().commit();
            System.out.println(d.getId());
        } catch (Exception ex) {
            em.getTransaction().rollback();
        }
    }


    private static void deleteDish(Scanner sc) {
        System.out.print("Enter dish id: ");
        String sId = sc.nextLine();
        long id = Long.parseLong(sId);
        Dish d = em.getReference(Dish.class, id);
        if (d == null) {
            System.out.println("Dish not found!");
            return;
        }
        em.getTransaction().begin();
        try {
            em.remove(d);
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
        }
    }

    private static void changeDish(Scanner sc) {
        System.out.print("Enter dish id: ");
        String sId = sc.nextLine();
        int id = Integer.parseInt(sId);
        System.out.print("Enter new name: ");
        String name = sc.nextLine();
        System.out.print("Enter new price: ");
        String sPrice = sc.nextLine();
        double price = Double.parseDouble(sPrice);

        Dish d = null;
        try {
            Query query = em.createQuery("SELECT d FROM Dish d WHERE d.id = :id", Dish.class);
            query.setParameter("id", id);
            d = (Dish) query.getSingleResult();
        } catch (NoResultException ex) {
            System.out.println("Dish not found!");
            return;
        } catch (NonUniqueResultException ex) {
            System.out.println("Non unique result!");
            return;
        }
        em.getTransaction().begin();
        try {
            d.setName(name);
            d.setPrice(price);
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
        }
    }


    private static void insertRandomDishes(Scanner sc) {
        System.out.print("Enter dishes count: ");
        String sCount = sc.nextLine();
        int count = Integer.parseInt(sCount);

        em.getTransaction().begin();
        try {
            for (int i = 0; i < count; i++) {
                Dish d = new Dish(randomName());
                em.persist(d);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
        }
    }

    private static void viewDishes() {
        Query query = em.createQuery(
                "SELECT d FROM Dish d", Dish.class);
        List<Dish> list = (List<Dish>) query.getResultList();

        for (Dish d : list)
            System.out.println(d);
    }

    private static void viewDishesWithDiscount() {
        Query query = em.createQuery(
                "SELECT d FROM Dish d WHERE d.discount=true ", Dish.class);
        List<Dish> list = (List<Dish>) query.getResultList();

        for (Dish d : list)
            System.out.println(d);
    }

    private static void viewDishesWithPrice(Scanner sc) {
        System.out.println("Enter lowest prise");
        String sLowPrice = sc.nextLine();
        double lowPrice = Double.parseDouble(sLowPrice);

        System.out.println("Enter highest prise");
        String sHighPrice = sc.nextLine();
        double highPrice = Double.parseDouble(sHighPrice);

        Query query = em.createQuery("SELECT d FROM Dish d WHERE d.price>:lowPrice AND d.price<:highPrice ", Dish.class);
        query.setParameter("lowPrice", lowPrice);
        query.setParameter("highPrice", highPrice);
        List<Dish> list = (List<Dish>) query.getResultList();
        for (Dish d : list)
            System.out.println(d);
    }

    private static void viewDishesWithWeight(Scanner sc) {
        double sumWeight = 0;
        boolean b = true;
        List<Dish> list = new ArrayList<>();

        do {
            System.out.println("Enter dish id: ");
            String sId = sc.nextLine();
            int id = Integer.parseInt(sId);

            Query query = em.createQuery("SELECT d FROM Dish d WHERE d.id=:id ", Dish.class);
            query.setParameter("id", id);
            Dish d = (Dish) query.getSingleResult();
            if (sumWeight + d.getWeight() > 1000) {
                System.out.println("Can't add dish with weight " + d.getWeight() +
                                "to order with weight " + sumWeight + ", total weight will be more than 1 kilo");
                b = false;
                continue;
            }
            list.add(d);
            sumWeight = sumWeight + d.getWeight();
            System.out.println("Dish added to order. Total weight is " + sumWeight);


        }while (b);

        for (Dish d : list)
            System.out.println(d);
    }


    static final String[] NAMES = {"Soup", "Steak", "Burger", "Tea", "Coffee"};
    static final Random RND = new Random();

    static String randomName() {
        return NAMES[RND.nextInt(NAMES.length)];
    }
}


