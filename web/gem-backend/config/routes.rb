Rails.application.routes.draw do
  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html

  get 'notepads/summary', to: 'notepads#summary'
  resources :notepads, only: [:index, :show, :create, :update]

  resources :equations, only: [:index, :create, :update, :destroy]
end
